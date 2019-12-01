package engine.acceptance

import assertk.assertThat
import engine.action.PassPriority
import engine.assertions.actionResultsInState
import engine.factories.DeckFactory
import engine.factories.PlayerStateFactory
import engine.factories.PlayerStateFactory.ID_ALICE
import engine.factories.PlayerStateFactory.ID_BOB
import engine.reducer.masterReducer
import engine.state.BeginningPhase
import engine.state.DrawStep
import engine.state.GameState
import engine.state.Turn
import engine.state.UpkeepStep
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("504. Draw Step")
class DrawStepTest {
    private val reducer = masterReducer()
    @Test
    fun `on the first turn, active player does not draw a card`() {
        assertThat(
            GameState(
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
        ).actionResultsInState(
            action = PassPriority(ID_BOB),
            expectedState = GameState(
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
        )
    }

    @Test
    fun `when it's not the first turn, active player draws a card`() {
        assertThat(
            GameState(
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
        ).actionResultsInState(
            action = PassPriority(ID_ALICE),
            expectedState = GameState(
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
        )
    }
}
