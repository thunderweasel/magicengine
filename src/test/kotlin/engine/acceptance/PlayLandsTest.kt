package engine.acceptance

import assertk.assertThat
import engine.action.PlayLand
import engine.assertions.actionResultsInError
import engine.assertions.actionResultsInState
import engine.cards.CardType
import engine.factories.DeckFactory
import engine.factories.PlayerStateFactory
import engine.factories.PlayerStateFactory.ID_ALICE
import engine.factories.PlayerStateFactory.ID_BOB
import engine.state.GameState
import engine.state.InvalidPlayerAction
import engine.state.Permanent
import engine.state.PreCombatMainPhase
import engine.state.StartingPlayerMustBeChosen
import engine.state.Turn
import engine.viewAs
import org.junit.jupiter.api.Test

class PlayLandsTest {
    @Test
    fun `Cannot play lands if the game hasn't started`() {
        val initialState = GameState(
            players = PlayerStateFactory.createAliceAndBobWithStartingHands(),
            temporalPosition = StartingPlayerMustBeChosen(ID_ALICE)
        )
        assertThat(initialState).actionResultsInError(
            action = PlayLand(ID_ALICE, 0),
            expectedError = InvalidPlayerAction.invalidTemporalState(
                action = PlayLand(ID_ALICE, 0),
                state = initialState
            )
        )
    }

    @Test
    fun `Cannot play lands if not the active player`() {
        val initialState = GameState(
            players = PlayerStateFactory.createAliceAndBobWithStartingHands(),
            temporalPosition = Turn(
                activePlayer = ID_ALICE,
                phase = PreCombatMainPhase,
                priority = ID_BOB
            )
        )
        assertThat(initialState).actionResultsInError(
            action = PlayLand(ID_BOB, 0),
            expectedError = InvalidPlayerAction(
                action = PlayLand(ID_BOB, 0),
                state = initialState,
                reason = "Player $ID_BOB is not the active player"
            )
        )
    }

    @Test
    fun `Cannot play lands if we don't have priority`() {
        val initialState = GameState(
            players = PlayerStateFactory.createAliceAndBobWithStartingHands(),
            temporalPosition = Turn(
                activePlayer = ID_ALICE,
                phase = PreCombatMainPhase,
                priority = ID_BOB
            )
        )
        assertThat(initialState).actionResultsInError(
            action = PlayLand(ID_ALICE, 0),
            expectedError = InvalidPlayerAction(
                action = PlayLand(ID_ALICE, 0),
                state = initialState,
                reason = "Player $ID_ALICE does not have priority"
            )
        )
    }

    @Test
    fun `Cannot play lands that are not known in the current view`() {
        val initialState = GameState(
            players = PlayerStateFactory.createAliceAndBobWithStartingHands(),
            temporalPosition = Turn(
                activePlayer = ID_ALICE,
                phase = PreCombatMainPhase,
                priority = ID_ALICE
            )
        ).viewAs(ID_BOB) // Alice's cards should be Unknown

        assertThat(initialState).actionResultsInError(
            action = PlayLand(ID_ALICE, 0),
            expectedError = InvalidPlayerAction(
                action = PlayLand(ID_ALICE, 0),
                state = initialState,
                reason = "Cannot play unknown card"
            )
        )
    }

    @Test
    fun `Playing a land causes a land permanent to be placed on the battlefield`() {
        val initialState = GameState(
            players = PlayerStateFactory.createAliceAndBobWithStartingHands(),
            temporalPosition = Turn(
                activePlayer = ID_ALICE,
                phase = PreCombatMainPhase,
                priority = ID_ALICE
            )
        )
        val alice = PlayerStateFactory.createAliceWithStartingHand()
        val expectedState = GameState(
            players = listOf(
                // Card should be removed from Alice's hand
                alice.copy(
                    hand = alice.hand.minus(alice.hand[0])
                ),
                PlayerStateFactory.createBobWithStartingHand()
            ),
            battlefield = listOf(
                Permanent(
                    name = DeckFactory.alice[0].name,
                    cardType = CardType.LAND,
                    subtype = "Forest",
                    card = DeckFactory.alice[0]
                )
            ),
            // Alice continues to hold priority
            temporalPosition = initialState.temporalPosition
        )
        assertThat(initialState).actionResultsInState(
            action = PlayLand(ID_ALICE, 0),
            expectedState = expectedState
        )
    }
}
