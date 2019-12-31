package engine.acceptance

import assertk.assertThat
import engine.action.ActivateAbility
import engine.assertions.actionResultsInError
import engine.assertions.actionResultsInState
import engine.cards.AbilitySpecId
import engine.cards.CardType
import engine.domain.ManaType
import engine.factories.GameStateFactory
import engine.factories.PlayerStateFactory
import engine.factories.PlayerStateFactory.ID_ALICE
import engine.factories.PlayerStateFactory.ID_BOB
import engine.state.ActivatedAbility
import engine.state.Card
import engine.state.EndStep
import engine.state.EndingPhase
import engine.state.InvalidPlayerAction
import engine.state.Permanent
import engine.state.Turn
import engine.state.createBattlefield
import engine.state.createManaPool
import org.junit.jupiter.api.Test

class ActivatingAbilitiesTest {
    private val initialState = GameStateFactory.create(
        battlefield = createBattlefield(
            Permanent(
                id = 1,
                name = "Forest",
                cardTypes = listOf(CardType.LAND),
                subtypes = listOf("Forest"),
                card = Card.KnownCard(
                    id = 0,
                    name = "Forest"
                ),
                activatedAbilities = listOf(
                    ActivatedAbility(
                        id = 1,
                        permanentId = 1,
                        specId = AbilitySpecId("Forest", 1)
                    )
                ),
                tapped = false,
                controller = ID_BOB
            )
        ),
        temporalPosition = Turn(
            activePlayer = ID_ALICE,
            phase = EndingPhase(step = EndStep),
            priority = ID_BOB
        )
    )

    @Test
    fun `cannot activate abilities on permanents that don't exist`() {
        assertThat(initialState).actionResultsInError(
            action = ActivateAbility(actingPlayer = ID_BOB, permanentId = 2, abilityId = 1),
            expectedError = InvalidPlayerAction(
                action = ActivateAbility(actingPlayer = ID_BOB, permanentId = 2, abilityId = 1),
                state = initialState,
                reason = "There is no permanent 2"
            )
        )
    }

    @Test
    fun `cannot activate abilities that don't exist`() {
        assertThat(initialState).actionResultsInError(
            action = ActivateAbility(actingPlayer = ID_BOB, permanentId = 1, abilityId = 2),
            expectedError = InvalidPlayerAction(
                action = ActivateAbility(actingPlayer = ID_BOB, permanentId = 1, abilityId = 2),
                state = initialState,
                reason = "There is no ability 2 on permanent 1"
            )
        )
    }

    @Test
    fun `cannot activate abilities on permanents we don't control`() {
        val state = initialState.copy(
            temporalPosition = Turn(
                activePlayer = ID_ALICE,
                phase = EndingPhase(step = EndStep),
                priority = ID_ALICE
            )
        )
        assertThat(state).actionResultsInError(
            action = ActivateAbility(actingPlayer = ID_ALICE, permanentId = 1, abilityId = 1),
            expectedError = InvalidPlayerAction(
                action = ActivateAbility(actingPlayer = ID_ALICE, permanentId = 1, abilityId = 1),
                state = state,
                reason = "Players cannot activate abilities on permanents they don't control"
            )
        )
    }

    @Test
    fun `cannot activate abilities if we don't have priority`() {
        val state = initialState.copy(
            temporalPosition = Turn(
                activePlayer = ID_ALICE,
                phase = EndingPhase(step = EndStep),
                priority = ID_ALICE
            )
        )
        assertThat(state).actionResultsInError(
            action = ActivateAbility(actingPlayer = ID_BOB, permanentId = 1, abilityId = 1),
            expectedError = InvalidPlayerAction(
                action = ActivateAbility(actingPlayer = ID_BOB, permanentId = 1, abilityId = 1),
                state = state,
                reason = "Player $ID_BOB does not have priority"
            )
        )
    }

    @Test
    fun `activating a mana ability causes mana to be added to its controller's mana pool without using the stack`() {
        assertThat(initialState).actionResultsInState(
            action = ActivateAbility(actingPlayer = ID_BOB, permanentId = 1, abilityId = 1),
            expectedState = initialState.copy(
                players = listOf(
                    PlayerStateFactory.createAliceWithStartingHand(),
                    PlayerStateFactory.createBobWithStartingHand().copy(
                        manaPool = createManaPool(
                            ManaType.GREEN to 1
                        )
                    )
                ),
                battlefield = createBattlefield(
                    Permanent(
                        id = 1,
                        name = "Forest",
                        cardTypes = listOf(CardType.LAND),
                        subtypes = listOf("Forest"),
                        card = Card.KnownCard(
                            id = 0,
                            name = "Forest"
                        ),
                        activatedAbilities = listOf(
                            ActivatedAbility(
                                id = 1,
                                permanentId = 1,
                                specId = AbilitySpecId("Forest", 1)
                            )
                        ),
                        tapped = true,
                        controller = ID_BOB
                    )
                )
            )
        )
    }
}
