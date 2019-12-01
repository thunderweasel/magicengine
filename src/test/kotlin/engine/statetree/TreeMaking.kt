package engine.statetree

import engine.action.PlayerAction
import engine.action.ResolvedRandomization
import engine.model.StatePendingRandomization
import engine.statetree.GameStateTree.OutcomeNode

class TreeMaking<STATE_TYPE : Any> private constructor() {
    companion object {
        fun <STATE_TYPE : Any> makeStateTree(makeRoot: TreeMaking<STATE_TYPE>.() -> OutcomeNode<STATE_TYPE>) =
            TreeMaking<STATE_TYPE>().makeRoot()
    }

    fun STATE_TYPE.thenBranch(vararg choices: GameStateTree.Edge.PlayerChoice<STATE_TYPE>): OutcomeNode.Resolved<STATE_TYPE> =
        OutcomeNode.Resolved(
            state = this,
            choices = choices.toList()
        )

    fun OutcomeNode.Resolved<STATE_TYPE>.thenBranch(vararg choices: GameStateTree.Edge.PlayerChoice<STATE_TYPE>): OutcomeNode.Resolved<STATE_TYPE> =
        copy(
            choices = this.choices.plus(choices)
        )

    fun OutcomeNode.PendingRandomization<STATE_TYPE>.thenBranch(vararg possibilities: GameStateTree.Edge.Possibility<STATE_TYPE>): OutcomeNode.PendingRandomization<STATE_TYPE> =
        copy(
            possibilities = this.possibilities.plus(possibilities)
        )

    fun STATE_TYPE.thenChain(vararg edges: GameStateTree.Edge<STATE_TYPE>): OutcomeNode<STATE_TYPE> =
        thenBranch(chain(*edges))

    fun OutcomeNode.Resolved<STATE_TYPE>.thenChain(vararg edges: GameStateTree.Edge<STATE_TYPE>): OutcomeNode<STATE_TYPE> =
        thenBranch(chain(*edges))

    fun OutcomeNode.PendingRandomization<STATE_TYPE>.thenChain(vararg edges: GameStateTree.Edge<STATE_TYPE>): OutcomeNode<STATE_TYPE> =
        thenBranch(chain(*edges))

    infix fun PlayerAction.resultsIn(outcome: OutcomeNode<STATE_TYPE>) =
        GameStateTree.Edge.PlayerChoice(action = this, expectedOutcome = outcome)

    infix fun PlayerAction.resultsIn(state: STATE_TYPE) =
        GameStateTree.Edge.PlayerChoice(
            action = this,
            expectedOutcome = OutcomeNode.Resolved(state = state)
        )

    infix fun PlayerAction.resultsIn(error: Throwable) =
        GameStateTree.Edge.PlayerChoice<STATE_TYPE>(
            action = this,
            expectedOutcome = OutcomeNode.CommandFailure(error = error)
        )

    infix fun ResolvedRandomization.resultsIn(outcome: OutcomeNode<STATE_TYPE>) =
        GameStateTree.Edge.Possibility(action = this, expectedOutcome = outcome)

    infix fun ResolvedRandomization.resultsIn(state: STATE_TYPE) =
        GameStateTree.Edge.Possibility(
            action = this,
            expectedOutcome = OutcomeNode.Resolved(state = state)
        )

    operator fun String.invoke(choice: GameStateTree.Edge.PlayerChoice<STATE_TYPE>): GameStateTree.Edge.PlayerChoice<STATE_TYPE> =
        choice.copy(description = this)

    operator fun String.invoke(possibility: GameStateTree.Edge.Possibility<STATE_TYPE>): GameStateTree.Edge.Possibility<STATE_TYPE> =
        possibility.copy(description = this)

    fun pendingRandomization(
        state: StatePendingRandomization<STATE_TYPE>? = null,
        vararg possibilities: GameStateTree.Edge.Possibility<STATE_TYPE>
    ): OutcomeNode.PendingRandomization<STATE_TYPE> =
        OutcomeNode.PendingRandomization(
            statePendingRandomization = state,
            possibilities = possibilities.toList()
        )

    private inline fun <reified T : GameStateTree.Edge<STATE_TYPE>> chain(vararg edges: GameStateTree.Edge<STATE_TYPE>): T {
        require(edges.isNotEmpty())
        val firstEdge = edges.reduceRight(::chainTwo)
        require(firstEdge is T)
        return firstEdge
    }

    private inline fun <reified T : GameStateTree.Edge<STATE_TYPE>> chainTwo(
        prev: GameStateTree.Edge<STATE_TYPE>,
        current: GameStateTree.Edge<STATE_TYPE>
    ): T {
        val prevOutcome = prev.expectedOutcome
        require(prevOutcome.edges.isEmpty()) { "Invalid tree - only the last outcome in a chain is allowed to have multiple branches" }
        return when (current) {
            is GameStateTree.Edge.PlayerChoice -> {
                require(prevOutcome is OutcomeNode.Resolved) { "Invalid tree - player choice can only follow resolved game state" }
                prev.withNewOutcome(
                    prevOutcome.copy(
                        choices = listOf(current)
                    )
                )
            }
            is GameStateTree.Edge.Possibility -> {
                require(prevOutcome is OutcomeNode.PendingRandomization) { "Invalid tree - random result can only follow pending randomization" }
                prev.withNewOutcome(
                    prevOutcome.copy(
                        possibilities = listOf(current)
                    )
                )
            }
        }
    }

    private inline fun <reified T : GameStateTree.Edge<STATE_TYPE>> GameStateTree.Edge<STATE_TYPE>.withNewOutcome(
        outcome: OutcomeNode<STATE_TYPE>
    ): T =
        when (this) {
            is GameStateTree.Edge.PlayerChoice -> copy(expectedOutcome = outcome)
            is GameStateTree.Edge.Possibility -> copy(expectedOutcome = outcome)
        } as T
}
