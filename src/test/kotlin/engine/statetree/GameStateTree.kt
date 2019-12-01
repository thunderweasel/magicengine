package engine.statetree

import engine.action.PlayerAction
import engine.action.ResolvedRandomization
import engine.state.StatePendingRandomization
import engine.statetree.GameStateTree.Edge.PlayerChoice
import engine.statetree.GameStateTree.Edge.Possibility

object GameStateTree {
    sealed class OutcomeNode<STATE_TYPE> {
        abstract val edges: List<Edge<STATE_TYPE>>
        data class Resolved<STATE_TYPE>(
            val state: STATE_TYPE,
            val choices: List<PlayerChoice<STATE_TYPE>> = emptyList()
        ) : OutcomeNode<STATE_TYPE>() {
            override val edges get() = choices
        }

        data class PendingRandomization<STATE_TYPE>(
            val statePendingRandomization: StatePendingRandomization<STATE_TYPE>? = null,
            val possibilities: List<Possibility<STATE_TYPE>>
        ) : OutcomeNode<STATE_TYPE>() {
            override val edges get() = possibilities
        }

        data class CommandFailure<STATE_TYPE>(
            val error: Throwable
        ) : OutcomeNode<STATE_TYPE>() {
            override val edges = emptyList<Edge<STATE_TYPE>>()
        }
    }

    sealed class Edge<STATE_TYPE> {
        abstract val expectedOutcome: OutcomeNode<STATE_TYPE>
        abstract val description: String?
        data class PlayerChoice<STATE_TYPE>(
            override val description: String? = null,
            val action: PlayerAction,
            override val expectedOutcome: OutcomeNode<STATE_TYPE>
        ) : Edge<STATE_TYPE>()

        data class Possibility<STATE_TYPE>(
            override val description: String? = null,
            val action: ResolvedRandomization,
            override val expectedOutcome: OutcomeNode<STATE_TYPE>
        ) : Edge<STATE_TYPE>()
    }
}
