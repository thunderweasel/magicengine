package engine.random

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import engine.action.GameAction
import engine.action.PendingRandomization
import engine.action.PerformMulligans
import engine.action.RandomizedResultAction
import engine.action.ResolvedRandomization
import engine.state.Card
import engine.state.Card.KnownCard
import engine.state.RandomRequest
import engine.state.Range
import engine.state.StatePendingRandomization
import org.junit.jupiter.api.Test

class RandomizationResolverTest {
    private var reducerCalls = mutableListOf<Pair<StatePendingRandomization<String>, GameAction>>()
    private var statesToReturn = mutableListOf<StatePendingRandomization<String>>()
    private val sut = RandomizationResolver(
        shuffler = CheatShuffler(cheat = ShuffleCheat.MoveOneCardToBottom),
        randomizer = FakeRandomizer(
            listOf(1, 2, 3, 4, 5)
        ),
        reducer = { state: StatePendingRandomization<String>, action ->
            reducerCalls.add(state to action)
            val returnedState = statesToReturn.first()
            statesToReturn.removeAt(0)
            returnedState
        }
    )

    @Test
    fun `if the state has no pending randomization, just return the state`() {
        val resolved = sut.resolve(StatePendingRandomization(gameState = "state", pendingAction = null))

        assertThat(reducerCalls).isEmpty()
        assertThat(resolved).isEqualTo("state")
    }

    @Test
    fun `if the state has pending randomization, send action with random results`() {
        val initialState = StatePendingRandomization(
            gameState = "state",
            pendingAction = pendingAction(
                shuffles = listOf(
                    listOf(KnownCard("1"), KnownCard("2"), KnownCard("3")),
                    listOf(KnownCard("A"), KnownCard("B"), KnownCard("C"))
                ),
                randomNumbers = listOf(1..2, 2..3)
            )
        )
        statesToReturn.add(StatePendingRandomization("new state", pendingAction = null))

        val resolved = sut.resolve(initialState)

        assertThat(reducerCalls).isEqualTo(
            listOf(
                initialState to randomizedResultAction(
                    // Deterministic due to shuffle cheating
                    completedShuffles = listOf(
                        listOf(KnownCard("2"), KnownCard("3"), KnownCard("1")),
                        listOf(KnownCard("B"), KnownCard("C"), KnownCard("A"))
                    ),
                    generatedNumbers = listOf(1, 2)
                )
            )
        )
        assertThat(resolved).isEqualTo("new state")
    }

    @Test
    fun `if the reducer returns a pending state again, it keeps sending more actions until it has no pending action`() {
        val initialState = StatePendingRandomization(
            gameState = "state 1",
            pendingAction = pendingAction(randomNumbers = listOf(1..2))
        )
        val expectedNewStates = listOf(
            StatePendingRandomization(
                "state 2",
                pendingAction = pendingAction(randomNumbers = listOf(2..3))
            ),
            StatePendingRandomization(
                "state 3",
                pendingAction = pendingAction(randomNumbers = listOf(3..5))
            ),
            StatePendingRandomization("final state", pendingAction = null)
        )
        statesToReturn.addAll(expectedNewStates)

        val resolved = sut.resolve(initialState)

        assertThat(reducerCalls).isEqualTo(
            listOf(
                initialState to randomizedResultAction(generatedNumbers = listOf(1)),
                expectedNewStates[0] to randomizedResultAction(generatedNumbers = listOf(2)),
                expectedNewStates[1] to randomizedResultAction(generatedNumbers = listOf(3))
            )
        )
        assertThat(resolved).isEqualTo("final state")
    }

    private fun pendingAction(shuffles: List<List<Card>> = emptyList(), randomNumbers: List<IntRange> = emptyList()) =
        PendingRandomization(
            actionOnResolution = PerformMulligans,
            request = RandomRequest(
                shuffles = shuffles,
                randomNumbers = randomNumbers.map { Range(it) }
            )
        )

    private fun randomizedResultAction(
        completedShuffles: List<List<Card>> = emptyList(),
        generatedNumbers: List<Int> = emptyList()
    ) =
        RandomizedResultAction(
            innerAction = PerformMulligans,
            resolvedRandomization = ResolvedRandomization(
                completedShuffles = completedShuffles,
                generatedNumbers = generatedNumbers
            )
        ) as GameAction
}
