package engine.acceptance

import assertk.assertThat
import assertk.assertions.isEqualTo
import engine.action.PassPriority
import engine.domain.ManaType
import engine.factories.GameStateFactory
import engine.factories.PlayerStateFactory
import engine.factories.PlayerStateFactory.ID_ALICE
import engine.factories.PlayerStateFactory.ID_BOB
import engine.formats.EverythingIsAForest
import engine.reducer.masterReducer
import engine.state.BeginningOfCombatStep
import engine.state.BeginningPhase
import engine.state.CombatPhase
import engine.state.DeclareAttackersStep
import engine.state.DrawStep
import engine.state.EndOfCombatStep
import engine.state.EndStep
import engine.state.EndingPhase
import engine.state.PostCombatMainPhase
import engine.state.PreCombatMainPhase
import engine.state.Turn
import engine.state.TurnPhase
import engine.state.UpkeepStep
import engine.state.createManaPool
import engine.state.noPendingRandomization
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class ManaPoolEmptyTest {
    @ParameterizedTest(name = "Mana pool empties at the end of {0}")
    @MethodSource("createTestArguments")
    fun manaPoolEmptiesAtTheEndOfTurnOrStep(phaseOrStep: TurnPhase) {
        val initialState = GameStateFactory.create(
            players = listOf(
                PlayerStateFactory.createAliceWithStartingHand().copy(
                    manaPool = createManaPool(ManaType.BLUE to 2)
                ),
                PlayerStateFactory.createBobWithStartingHand()
            ),
            temporalPosition = Turn(
                activePlayer = ID_ALICE,
                phase = phaseOrStep,
                priority = ID_BOB
            )
        )
        val newState =
            masterReducer(format = EverythingIsAForest())(initialState.noPendingRandomization(), PassPriority(ID_BOB))
        assertThat(newState.gameState.players[0].manaPool).isEqualTo(createManaPool())
    }

    companion object {
        @JvmStatic
        fun createTestArguments() =
            // All steps where players can receive priority
            listOf(
                BeginningPhase(step = UpkeepStep),
                BeginningPhase(step = DrawStep),
                PreCombatMainPhase,
                CombatPhase(step = BeginningOfCombatStep),
                CombatPhase(step = DeclareAttackersStep),
                CombatPhase(step = EndOfCombatStep),
                PostCombatMainPhase,
                EndingPhase(step = EndStep)
            ).map {
                Arguments.of(it)
            }
    }
}
