package engine.statetree

import engine.action.RandomizedResultAction
import engine.model.StatePendingRandomization
import engine.model.noPendingRandomization
import engine.reducer.StatePendingRandomizationReducer
import engine.statetree.GameStateTree.Edge
import engine.statetree.GameStateTree.OutcomeNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

abstract class StateTreeTest<STATE_TYPE : Any>(
    val reducer: StatePendingRandomizationReducer<STATE_TYPE>,
    val root: OutcomeNode<STATE_TYPE>
) {
    @TestFactory
    fun verifyAllTransitions(): Iterator<DynamicNode> {
        require(
            root is OutcomeNode.Resolved ||
                (root is OutcomeNode.PendingRandomization && root.statePendingRandomization != null)
        ) {
            "Root node must have an initial state"
        }
        return createTests(node = root).iterator()
    }

    private fun createTests(
        node: OutcomeNode<STATE_TYPE>,
        computePreviousState: () -> StatePendingRandomization<STATE_TYPE>? = { null }
    ): Sequence<DynamicNode> = sequence {
        node.edges.forEach { edge: Edge<STATE_TYPE> ->
            yield(DynamicTest.dynamicTest(edge.description ?: edge.generateDescription()) {
                runTest(getPreviousState(node, computePreviousState), edge)
            })
            createTests(
                edge.expectedOutcome,
                computePreviousState = {
                    getPreviousState(node, computePreviousState)?.let { previousState ->
                        kotlin.runCatching { computeState(previousState, edge) }.getOrNull()
                    }
                }
            )
                .forEach { yield(it) }
        }
    }

    private fun getPreviousState(
        node: OutcomeNode<STATE_TYPE>,
        computePreviousState: () -> StatePendingRandomization<STATE_TYPE>?
    ): StatePendingRandomization<STATE_TYPE>? {
        // Use the expected previous state if available so that we don't have to rely on computed state if a previous
        // test fails.
        return when {
            node is OutcomeNode.Resolved -> node.state.noPendingRandomization()
            node is OutcomeNode.PendingRandomization && node.statePendingRandomization != null -> node.statePendingRandomization
            else -> computePreviousState()
        }
    }

    private fun Edge<STATE_TYPE>.generateDescription() =
        when (this) {
            is Edge.PlayerChoice<STATE_TYPE> -> "$action"
            is Edge.Possibility<STATE_TYPE> -> "$action"
        }

    private fun computeState(
        previousState: StatePendingRandomization<STATE_TYPE>,
        edge: Edge<STATE_TYPE>
    ) = when (edge) {
        is Edge.PlayerChoice -> reducer(previousState, edge.action)
        is Edge.Possibility -> {
            val pendingAction = previousState.pendingAction
            require(pendingAction != null)
            reducer(
                previousState,
                RandomizedResultAction(
                    innerAction = pendingAction.actionOnResolution,
                    resolvedRandomization = edge.action
                )
            )
        }
    }

    private fun runTest(
        previousState: StatePendingRandomization<STATE_TYPE>?,
        edge: Edge<STATE_TYPE>
    ) {
        require(previousState != null) {
            "Previous state unavailable, possibly due to earlier failure"
        }
        val newState = kotlin.runCatching { computeState(previousState, edge) }

        when (val expectedOutcome = edge.expectedOutcome) {
            is OutcomeNode.PendingRandomization -> {
                assertThat(newState.isSuccess)
                    .describedAs("Expecting success")
                    .isTrue()
                assertThat(newState.getOrNull()?.pendingAction).isNotNull()
            }
            is OutcomeNode.Resolved -> {
                assertThat(newState.isSuccess)
                    .describedAs("Expecting success")
                    .isTrue()
                assertThat(newState.getOrNull()?.pendingAction).isNull()
                assertThat(newState.getOrNull()?.gameState).isEqualTo(expectedOutcome.state)
            }
            is OutcomeNode.CommandFailure -> {
                assertThat(newState.exceptionOrNull())
                    .describedAs("Expecting error")
                    .isEqualTo(expectedOutcome.error)
            }
        }
    }
}
