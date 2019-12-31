package engine.state

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import engine.action.PendingRandomization
import engine.action.PerformMulligans
import engine.factories.DeckFactory
import engine.factories.GameStateFactory
import engine.factories.PlayerStateFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.junit.jupiter.api.Test

class GameStateSerializationTest {
    private val exampleState =
        GameStatePendingRandomization(
            gameState = GameStateFactory.create(
                players = listOf(
                    PlayerState(
                        id = PlayerStateFactory.ID_ALICE,
                        library = DeckFactory.alice.slice(7..59),
                        lifeTotal = 18,
                        hand = DeckFactory.alice.slice(0..6)
                    ),
                    PlayerState(
                        id = PlayerStateFactory.ID_BOB,
                        library = DeckFactory.bob.slice(7..59),
                        lifeTotal = 12,
                        hand = DeckFactory.bob.slice(0..6)
                    )
                ),
                temporalPosition = ResolvingMulligans(
                    numberOfMulligans = 4,
                    startingPlayer = PlayerStateFactory.ID_BOB,
                    turnToDecide = PlayerStateFactory.ID_BOB,
                    mulliganDecisions = mapOf(
                        PlayerStateFactory.ID_ALICE to MulliganDecision.UNDECIDED,
                        PlayerStateFactory.ID_BOB to MulliganDecision.KEEP
                    )
                )
            ),
            pendingAction = PendingRandomization(
                actionOnResolution = PerformMulligans,
                request = RandomRequest()
            )
        )

    @Test
    fun `game state can be serialized as JSON and then de-serialized`() {
        val json = Json(JsonConfiguration.Stable)
        val jsonString = json.stringify(StatePendingRandomization.serializer(GameState.serializer()), exampleState)
        val parsed = json.parse(StatePendingRandomization.serializer(GameState.serializer()), jsonString)

        assertThat(parsed).isDataClassEqualTo(exampleState)
    }
}
