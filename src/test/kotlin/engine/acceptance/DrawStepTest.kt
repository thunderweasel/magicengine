package engine.acceptance

import assertk.assertThat
import engine.acceptance.DrawStepStates.drawStepTurn1
import engine.acceptance.DrawStepStates.drawStepTurn2
import engine.acceptance.DrawStepStates.upkeepAlicePriorityTurn2
import engine.acceptance.DrawStepStates.upkeepBobPriorityTurn1
import engine.action.PassPriority
import engine.assertions.matchesState
import engine.factories.DeckFactory
import engine.factories.PlayerStateFactory
import engine.factories.PlayerStateFactory.ID_ALICE
import engine.factories.PlayerStateFactory.ID_BOB
import engine.model.BeginningPhase
import engine.model.DrawStep
import engine.model.GameState
import engine.model.Turn
import engine.model.UpkeepStep
import engine.model.noPendingRandomization
import engine.reducer.masterReducer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("504. Draw Step")
class DrawStepTest {
    private val reducer = masterReducer()
    @Test
    fun `on the first turn, active player does not draw a card`() {
        assertThat(
            reducer(upkeepBobPriorityTurn1.noPendingRandomization(), PassPriority(ID_BOB))
        ).matchesState(drawStepTurn1)
    }

    @Test
    fun `when it's not the first turn, active player draws a card`() {
        assertThat(
            reducer(upkeepAlicePriorityTurn2.noPendingRandomization(), PassPriority(ID_ALICE))
        ).matchesState(drawStepTurn2)
    }
}

private object DrawStepStates {
    val upkeepBobPriorityTurn1 = GameState(
        players = listOf(
            PlayerStateFactory.createAlice(hand = DeckFactory.alice.slice(0..6)),
            PlayerStateFactory.createBob(hand = DeckFactory.bob.slice(0..6))
        ),
        temporalPosition = Turn(
            activePlayer = ID_ALICE,
            phase = BeginningPhase(step = UpkeepStep),
            priority = ID_BOB,
            firstTurn = true
        )
    )

    val drawStepTurn1 = GameState(
        players = listOf(
            PlayerStateFactory.createAlice(hand = DeckFactory.alice.slice(0..6)),
            PlayerStateFactory.createBob(hand = DeckFactory.bob.slice(0..6))
        ),
        temporalPosition = Turn(
            activePlayer = ID_ALICE,
            phase = BeginningPhase(step = DrawStep),
            priority = ID_ALICE,
            firstTurn = true
        )
    )

    val upkeepAlicePriorityTurn2 = GameState(
        players = listOf(
            PlayerStateFactory.createAlice(hand = DeckFactory.alice.slice(0..6)),
            PlayerStateFactory.createBob(hand = DeckFactory.bob.slice(0..6))
        ),
        temporalPosition = Turn(
            activePlayer = ID_BOB,
            phase = BeginningPhase(step = UpkeepStep),
            priority = ID_ALICE,
            firstTurn = false
        )
    )

    val drawStepTurn2 = GameState(
        players = listOf(
            PlayerStateFactory.createAlice(hand = DeckFactory.alice.slice(0..6)),
            PlayerStateFactory.createBob(hand = DeckFactory.bob.slice(0..7)) // draw a card
        ),
        temporalPosition = Turn(
            activePlayer = ID_BOB,
            phase = BeginningPhase(step = DrawStep),
            priority = ID_BOB,
            firstTurn = false
        )
    )
}
