package engine.acceptance

import assertk.assertThat
import engine.action.PassPriority
import engine.assertions.actionResultsInState
import engine.factories.GameStateFactory
import engine.factories.PermanentFactory
import engine.factories.PlayerStateFactory.ID_ALICE
import engine.factories.PlayerStateFactory.ID_BOB
import engine.state.BeginningPhase
import engine.state.Card
import engine.state.EndStep
import engine.state.EndingPhase
import engine.state.Turn
import engine.state.UpkeepStep
import engine.state.createBattlefield
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("502. Untap Step")
class UntapStepTest {
    private val permanentFactory = PermanentFactory()

    @Test
    fun `all of the active players permanents untap at the beginning of the turn, unless an effect says otherwise`() {
        assertThat(
            GameStateFactory.create(
                battlefield = createBattlefield(
                    permanentFactory.createBasicLand(
                        card = Card.KnownCard(1, "Forest"),
                        permanentId = 1,
                        controller = ID_BOB,
                        tapped = true
                    ),
                    permanentFactory.createBasicLand(
                        card = Card.KnownCard(1, "Forest"),
                        permanentId = 2,
                        controller = ID_ALICE,
                        tapped = true
                    ),
                    permanentFactory.createBasicLand(
                        card = Card.KnownCard(1, "Forest"),
                        permanentId = 3,
                        controller = ID_BOB,
                        tapped = true
                    )
                ),
                temporalPosition = Turn(
                    activePlayer = ID_ALICE,
                    phase = EndingPhase(step = EndStep),
                    priority = ID_BOB
                )
            )
        ).actionResultsInState(
            action = PassPriority(ID_BOB),
            expectedState = GameStateFactory.create(
                battlefield = createBattlefield(
                    permanentFactory.createBasicLand(
                        card = Card.KnownCard(1, "Forest"),
                        permanentId = 1,
                        controller = ID_BOB,
                        tapped = false
                    ),
                    permanentFactory.createBasicLand(
                        card = Card.KnownCard(1, "Forest"),
                        permanentId = 2,
                        controller = ID_ALICE,
                        tapped = true // Should not untap the non-active player's permanents
                    ),
                    permanentFactory.createBasicLand(
                        card = Card.KnownCard(1, "Forest"),
                        permanentId = 3,
                        controller = ID_BOB,
                        tapped = false
                    )
                ),
                temporalPosition = Turn(
                    activePlayer = ID_BOB,
                    phase = BeginningPhase(step = UpkeepStep), // Players do not receive priority on untap step
                    priority = ID_BOB
                )
            )
        )
    }
}
