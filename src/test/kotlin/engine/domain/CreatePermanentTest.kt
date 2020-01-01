package engine.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import engine.cards.AbilitySpecId
import engine.cards.CardType
import engine.cards.ForestSpec
import engine.factories.GameStateFactory
import engine.factories.PlayerStateFactory.ID_ALICE
import engine.state.ActivatedAbility
import engine.state.Card
import engine.state.Permanent
import engine.state.PostCombatMainPhase
import engine.state.Turn
import engine.state.createBattlefield
import org.junit.jupiter.api.Test

internal class CreatePermanentTest {
    private val forestCard = Card.KnownCard(1, "Forest")
    private val initialState = GameStateFactory.create(
        temporalPosition = Turn(
            activePlayer = ID_ALICE,
            phase = PostCombatMainPhase,
            priority = ID_ALICE
        )
    )

    @Test
    fun `it adds the permanent to the game state`() {
        val newState = initialState.createPermanent(
            card = forestCard,
            cardSpec = ForestSpec,
            controller = ID_ALICE
        )

        assertThat(newState.battlefield).isEqualTo(
            createBattlefield(
                Permanent(
                    id = 1,
                    name = "Forest",
                    cardTypes = listOf(CardType.LAND),
                    subtypes = listOf("Forest"),
                    card = forestCard,
                    activatedAbilities = listOf(
                        ActivatedAbility(
                            id = 1,
                            permanentId = 1,
                            specId = AbilitySpecId("Forest", 1)
                        )
                    ),
                    tapped = false,
                    controller = ID_ALICE
                )
            )
        )
    }

    @Test
    fun `when multiple permanents are created, ensure that they have unique IDs`() {
        val newState = (1..3).fold(initialState) { state, _ ->
            state.createPermanent(
                card = forestCard,
                cardSpec = ForestSpec,
                controller = ID_ALICE
            )
        }

        assertThat(newState.battlefield).isEqualTo(
            createBattlefield(
                Permanent(
                    id = 1,
                    name = "Forest",
                    cardTypes = listOf(CardType.LAND),
                    subtypes = listOf("Forest"),
                    card = forestCard,
                    activatedAbilities = listOf(
                        ActivatedAbility(
                            id = 1,
                            permanentId = 1,
                            specId = AbilitySpecId("Forest", 1)
                        )
                    ),
                    tapped = false,
                    controller = ID_ALICE
                ),
                Permanent(
                    id = 2,
                    name = "Forest",
                    cardTypes = listOf(CardType.LAND),
                    subtypes = listOf("Forest"),
                    card = forestCard,
                    activatedAbilities = listOf(
                        ActivatedAbility(
                            id = 1,
                            permanentId = 2,
                            specId = AbilitySpecId("Forest", 1)
                        )
                    ),
                    tapped = false,
                    controller = ID_ALICE
                ),
                Permanent(
                    id = 3,
                    name = "Forest",
                    cardTypes = listOf(CardType.LAND),
                    subtypes = listOf("Forest"),
                    card = forestCard,
                    activatedAbilities = listOf(
                        ActivatedAbility(
                            id = 1,
                            permanentId = 3,
                            specId = AbilitySpecId("Forest", 1)
                        )
                    ),
                    tapped = false,
                    controller = ID_ALICE
                )
            )
        )
    }
}
