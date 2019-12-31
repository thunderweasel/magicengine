package engine.acceptance

import assertk.assertThat
import engine.action.PassPriority
import engine.action.PlayLand
import engine.assertions.actionResultsInError
import engine.assertions.actionResultsInState
import engine.cards.AbilitySpecId
import engine.cards.CardType
import engine.factories.DeckFactory
import engine.factories.GameStateFactory
import engine.factories.PlayerStateFactory
import engine.factories.PlayerStateFactory.ID_ALICE
import engine.factories.PlayerStateFactory.ID_BOB
import engine.formats.EverythingIsAForest
import engine.reducer.masterReducer
import engine.reducer.turnPhases
import engine.state.ActivatedAbility
import engine.state.BeginningPhase
import engine.state.EndStep
import engine.state.EndingPhase
import engine.state.InvalidPlayerAction
import engine.state.Permanent
import engine.state.PostCombatMainPhase
import engine.state.PreCombatMainPhase
import engine.state.StartingPlayerMustBeChosen
import engine.state.Turn
import engine.state.TurnHistory
import engine.state.TurnPhase
import engine.state.UpkeepStep
import engine.state.createBattlefield
import engine.state.noPendingRandomization
import engine.viewAs
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

@DisplayName("305. Playing lands")
class PlayLandsTest {
    private val reducer = masterReducer(format = EverythingIsAForest())
    @Test
    fun `Cannot play lands if the game hasn't started`() {
        val initialState = GameStateFactory.create(
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
        val initialState = GameStateFactory.create(
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
        val initialState = GameStateFactory.create(
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
        val initialState = GameStateFactory.create(
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

    private val successfullyPlayedLand by lazy {
        val alice = PlayerStateFactory.createAliceWithStartingHand()
        GameStateFactory.create(
            players = listOf(
                // Card should be removed from Alice's hand
                alice.copy(
                    hand = alice.hand.minus(alice.hand[0])
                ),
                PlayerStateFactory.createBobWithStartingHand()
            ),
            battlefield = createBattlefield(
                Permanent(
                    id = 1,
                    name = DeckFactory.alice[0].name,
                    cardTypes = listOf(CardType.LAND),
                    subtypes = listOf("Forest"),
                    card = DeckFactory.alice[0],
                    controller = ID_ALICE,
                    activatedAbilities = listOf(ActivatedAbility(
                        id = 1,
                        permanentId = 1,
                        specId = AbilitySpecId("Forest", 1)
                    ))
                )
            ),
            temporalPosition = Turn(
                activePlayer = ID_ALICE,
                phase = PreCombatMainPhase,
                // Alice continues to hold priority after playing a land
                priority = ID_ALICE,
                // Record that we've played a land
                history = TurnHistory(numberOfLandsPlayed = 1)
            )
        )
    }

    @Test
    fun `Playing a land causes a land permanent to be placed on the battlefield`() {
        val initialState = GameStateFactory.create(
            players = PlayerStateFactory.createAliceAndBobWithStartingHands(),
            temporalPosition = Turn(
                activePlayer = ID_ALICE,
                phase = PreCombatMainPhase,
                priority = ID_ALICE
            )
        )
        assertThat(initialState).actionResultsInState(
            action = PlayLand(ID_ALICE, 0),
            expectedState = successfullyPlayedLand
        )
    }

    @Test
    fun `Cannot play multiple lands on the same turn`() {
        val initialState = reducer(
            GameStateFactory.create(
                players = PlayerStateFactory.createAliceAndBobWithStartingHands(),
                temporalPosition = Turn(
                    activePlayer = ID_ALICE,
                    phase = PreCombatMainPhase,
                    priority = ID_ALICE
                )
            ).noPendingRandomization(), PlayLand(ID_ALICE, 0)
        )

        assertThat(initialState.gameState).actionResultsInError(
            action = PlayLand(ID_ALICE, 0),
            expectedError = InvalidPlayerAction(
                action = PlayLand(ID_ALICE, 0),
                state = initialState.gameState,
                reason = "Player $ID_ALICE cannot play another land this turn"
            )
        )
    }

    @Test
    fun `Number of lands played is reset when the turn ends`() {
        val initialState = GameStateFactory.create(
            players = PlayerStateFactory.createAliceAndBobWithStartingHands(),
            temporalPosition = Turn(
                activePlayer = ID_ALICE,
                phase = EndingPhase(step = EndStep),
                priority = ID_BOB,
                history = TurnHistory(numberOfLandsPlayed = 1)
            )
        )

        assertThat(initialState).actionResultsInState(
            action = PassPriority(ID_BOB),
            expectedState = GameStateFactory.create(
                players = PlayerStateFactory.createAliceAndBobWithStartingHands(),
                temporalPosition = Turn(
                    activePlayer = ID_BOB,
                    phase = BeginningPhase(step = UpkeepStep),
                    priority = ID_BOB,
                    history = TurnHistory(numberOfLandsPlayed = 0)
                )
            )
        )
    }

    @TestFactory
    fun `Can only play lands during the main phases`(): List<DynamicNode> {
        fun createInitialState(phase: TurnPhase) = GameStateFactory.create(
            players = PlayerStateFactory.createAliceAndBobWithStartingHands(),
            temporalPosition = Turn(
                activePlayer = ID_ALICE,
                phase = phase,
                priority = ID_ALICE,
                history = TurnHistory(numberOfLandsPlayed = 0)
            )
        )

        val action = PlayLand(ID_ALICE, indexInHand = 0)

        return turnPhases.map { phase ->
            if (phase == PreCombatMainPhase || phase == PostCombatMainPhase) {
                DynamicTest.dynamicTest("Can play lands when phase is $phase") {
                    val initialState = createInitialState(phase = phase)
                    assertThat(initialState).actionResultsInState(
                        action = action,
                        expectedState = successfullyPlayedLand.copy(
                            temporalPosition = Turn(
                                activePlayer = ID_ALICE,
                                phase = phase,
                                priority = ID_ALICE,
                                history = TurnHistory(numberOfLandsPlayed = 1)
                            )
                        )
                    )
                }
            } else {
                DynamicTest.dynamicTest("Cannot play lands when phase is $phase") {
                    val initialState = createInitialState(phase = phase)
                    assertThat(initialState).actionResultsInError(
                        action = action,
                        expectedError = InvalidPlayerAction(
                            action = action,
                            state = initialState,
                            reason = "Lands can only be played during the active player's main phase"
                        )
                    )
                }
            }
        }
    }
}
