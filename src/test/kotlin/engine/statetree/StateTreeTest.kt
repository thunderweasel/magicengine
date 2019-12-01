package engine.statetree

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import assertk.assertions.support.expected
import assertk.assertions.support.show
import engine.action.RandomizedResultAction
import engine.reducer.StatePendingRandomizationReducer
import engine.state.StatePendingRandomization
import engine.state.noPendingRandomization
import engine.statetree.GameStateTree.Edge
import engine.statetree.GameStateTree.OutcomeNode
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
        assertThat(newState).matchesOutcome(edge.expectedOutcome)
    }

    private fun Assert<Result<StatePendingRandomization<STATE_TYPE>>>.matchesOutcome(expected: OutcomeNode<STATE_TYPE>) =
        when (expected) {
            is OutcomeNode.CommandFailure -> {
                prop(Result<StatePendingRandomization<STATE_TYPE>>::exceptionOrNull)
                    .isEqualTo(expected.error)
            }
            is OutcomeNode.Resolved -> given { actual ->
                val error = actual.exceptionOrNull()
                val pendingAction = actual.getOrNull()?.pendingAction
                val expectedPrefix = "resolved state: ${show(expected.state)}"
                if (error != null) {
                    expected("$expectedPrefix, but an error was returned instead: ${show(error)}")
                }
                if (pendingAction != null) {
                    expected("$expectedPrefix, but the actual result is pending randomization: ${show(actual.getOrNull())}")
                }
                prop("getOrNull") { actual.getOrNull() }
                    .prop(StatePendingRandomization<STATE_TYPE>::gameState)
                    .isEqualTo(expected.state)
            }
            is OutcomeNode.PendingRandomization -> given { actual ->
                val error = actual.exceptionOrNull()
                val pendingAction = actual.getOrNull()?.pendingAction
                if (error != null) {
                    expected("pending randomization, but an error was returned instead: ${show(error)}")
                }
                if (pendingAction == null) {
                    expected("pending randomization, but resolved state was returned instead: ${show(actual.getOrNull()?.gameState)}")
                }
                if (expected.statePendingRandomization != null) {
                    prop("getOrNull") { actual.getOrNull() }
                        .isEqualTo(expected.statePendingRandomization)
                }
            }
        }
}
