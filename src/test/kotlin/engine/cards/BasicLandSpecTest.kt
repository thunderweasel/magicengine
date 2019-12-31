package engine.cards

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import engine.domain.ManaType
import engine.factories.GameStateFactory
import engine.factories.PermanentFactory
import engine.factories.PlayerStateFactory
import engine.factories.PlayerStateFactory.ID_ALICE
import engine.formats.AllSpellsAreBurnSpells
import engine.state.BeginningOfCombatStep
import engine.state.Card
import engine.state.CombatPhase
import engine.state.Turn
import engine.state.createBattlefield
import engine.state.createManaPool
import java.util.stream.Stream
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class BasicLandSpecTest {
    private val permanentFactory = PermanentFactory(format = AllSpellsAreBurnSpells())

    @ParameterizedTest(name = "{0} produces {1}")
    @MethodSource("createTestArguments")
    fun landProducesCorrectMana(name: String, manaType: ManaType) {
        val spec = allBasicLands.find { it.name == name }!!
        val initialState = createInitialState(spec)
        val newState =
            spec.manaAbility.resolve(initialState, initialState.battlefield.getValue(1).activatedAbilities[0])
        assertThat(newState.pendingAction).isNull()
        assertThat(newState.gameState.players[0].manaPool).isEqualTo(
            createManaPool(
                manaType to 1
            )
        )
    }

    companion object {
        @JvmStatic
        fun createTestArguments(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("Plains", ManaType.WHITE),
                Arguments.of("Island", ManaType.BLUE),
                Arguments.of("Swamp", ManaType.BLACK),
                Arguments.of("Mountain", ManaType.RED),
                Arguments.of("Forest", ManaType.GREEN)
            )
        }
    }

    private fun createInitialState(spec: BasicLandSpec) = GameStateFactory.create(
        players = PlayerStateFactory.createAliceAndBobWithStartingHands(),
        battlefield = createBattlefield(
            permanentFactory.createBasicLand(Card.KnownCard(0, spec.name), permanentId = 1, controller = ID_ALICE)
        ),
        temporalPosition = Turn(
            activePlayer = ID_ALICE,
            priority = ID_ALICE,
            phase = CombatPhase(step = BeginningOfCombatStep)
        )
    )
}
