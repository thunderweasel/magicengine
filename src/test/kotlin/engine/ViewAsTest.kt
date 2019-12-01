package engine

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import engine.factories.DeckFactory
import engine.factories.PlayerStateFactory
import engine.model.Card
import engine.model.GameState
import engine.model.MulliganDecision
import engine.model.PlayerState
import engine.model.ResolvingMulligans
import org.junit.jupiter.api.Test

class ViewAsTest {
    private val exampleState =
        GameState(
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
        )

    @Test
    fun `when viewing as Alice, hides information that should be hidden to Alice`() {
        assertThat(exampleState.viewAs(PlayerStateFactory.ID_ALICE)).isDataClassEqualTo(
            GameState(
                viewer = PlayerStateFactory.ID_ALICE,
                players = listOf(
                    PlayerState(
                        id = PlayerStateFactory.ID_ALICE,
                        // Alice cannot see the contents of her library
                        library = (7..59).map { Card.UnknownCard },
                        lifeTotal = 18,
                        // But she can see her own hand
                        hand = DeckFactory.alice.slice(0..6)
                    ),
                    PlayerState(
                        id = PlayerStateFactory.ID_BOB,
                        // Alice cannot see the contents of Bob's library
                        library = (7..59).map { Card.UnknownCard },
                        lifeTotal = 12,
                        // Nor can she see Bob's hand
                        hand = (0..6).map { Card.UnknownCard }
                    )
                ),
                // Mulligan state is all open information
                temporalPosition = ResolvingMulligans(
                    numberOfMulligans = 4,
                    startingPlayer = PlayerStateFactory.ID_BOB,
                    turnToDecide = PlayerStateFactory.ID_BOB,
                    mulliganDecisions = mapOf(
                        PlayerStateFactory.ID_ALICE to MulliganDecision.UNDECIDED,
                        PlayerStateFactory.ID_BOB to MulliganDecision.KEEP
                    )
                )
            )
        )
    }

    @Test
    fun `when viewing as Bob, hides information that should be hidden to Bob`() {
        assertThat(exampleState.viewAs(PlayerStateFactory.ID_BOB)).isDataClassEqualTo(
            GameState(
                viewer = PlayerStateFactory.ID_BOB,
                players = listOf(
                    PlayerState(
                        id = PlayerStateFactory.ID_ALICE,
                        // Bob cannot see the contents of Alice's library
                        library = (7..59).map { Card.UnknownCard },
                        lifeTotal = 18,
                        // Nor can he see Alice's hand
                        hand = (0..6).map { Card.UnknownCard }
                    ),
                    PlayerState(
                        id = PlayerStateFactory.ID_BOB,
                        // Bob cannot see the contents of his library
                        library = (7..59).map { Card.UnknownCard },
                        lifeTotal = 12,
                        // But he can see his own hand
                        hand = DeckFactory.bob.slice(0..6)
                    )
                ),
                // Mulligan state is all open information
                temporalPosition = ResolvingMulligans(
                    numberOfMulligans = 4,
                    startingPlayer = PlayerStateFactory.ID_BOB,
                    turnToDecide = PlayerStateFactory.ID_BOB,
                    mulliganDecisions = mapOf(
                        PlayerStateFactory.ID_ALICE to MulliganDecision.UNDECIDED,
                        PlayerStateFactory.ID_BOB to MulliganDecision.KEEP
                    )
                )
            )
        )
    }
}
