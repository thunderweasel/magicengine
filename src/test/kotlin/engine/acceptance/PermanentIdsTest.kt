package engine.acceptance

import assertk.assertThat
import assertk.assertions.hasSize
import engine.action.PassPriority
import engine.action.PlayLand
import engine.factories.DeckFactory
import engine.factories.GameStateFactory
import engine.factories.PlayerStateFactory.ID_ALICE
import engine.factories.PlayerStateFactory.ID_BOB
import engine.formats.AllSpellsAreBurnSpells
import engine.reducer.masterReducer
import engine.state.GameState
import engine.state.PostCombatMainPhase
import engine.state.PreCombatMainPhase
import engine.state.Turn
import engine.state.noPendingRandomization
import org.junit.jupiter.api.Test

class PermanentIdsTest {
    private val reducer = masterReducer(format = AllSpellsAreBurnSpells())
    @Test
    fun `when adding lands, should ensure the permanent IDs are unique`() {
        var state = GameStateFactory.create(
            temporalPosition = Turn(
                activePlayer = ID_ALICE,
                phase = PostCombatMainPhase,
                priority = ID_ALICE
            )
        ).noPendingRandomization()

        // Alice plays a land
        state = reducer(
            state,
            PlayLand(ID_ALICE, DeckFactory.alice[0].id)
        )

        // Pass priority until Bob can play a land
        while (
            state.gameState.turn().activePlayer != ID_BOB ||
            state.gameState.turn().priority != ID_BOB ||
            state.gameState.turn().phase != PreCombatMainPhase
        ) {
            state = reducer(
                state,
                PassPriority(state.gameState.turn().priority!!)
            )
        }

        // Bob plays a land
        state = reducer(
            state,
            PlayLand(ID_BOB, DeckFactory.bob[0].id)
        )

        assertThat(state.gameState.battlefield).hasSize(2)
        assertThat(state.gameState.battlefield.map { it.value.id }.toSet()).hasSize(2)
    }

    private fun GameState.turn(): Turn = temporalPosition as Turn
}
