package engine.acceptance

import engine.MagicEngine
import engine.action.PlayerAction
import engine.factories.DeckFactory
import engine.factories.PlayerStateFactory
import engine.model.Card
import engine.model.GameStart
import engine.model.GameStart.ResolvingMulligans
import engine.model.GameStart.StartingPlayerMustBeChosen
import engine.model.GameState
import engine.model.InvalidPlayerAction
import engine.model.MulliganDecision
import engine.model.PlayerState
import engine.random.CheatShuffler
import engine.random.FakeRandomizer
import engine.random.RandomizationResolver
import engine.random.ShuffleCheat
import engine.reducer.masterReducer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Starting the Game")
class StartingTheGameTest {
    private val engine = MagicEngine(
        randomizationResolver = RandomizationResolver(
            reducer = masterReducer(),
            shuffler = cheatShuffler,
            // Feeding fake values for random choices
            randomizer = FakeRandomizer(
                listOf(
                    PlayerStateFactory.ID_ALICE // rig the coin toss for Alice
                )
            )
        )
    )

    // 103.1, 103.2, 103.3 (implicit)
    @Test
    fun `at the start of the game, decks are shuffled and a random player gets to choose turn order`() {
        val gameState = engine.start2PlayerGame(
            deck1 = DeckFactory.alice,
            deck2 = DeckFactory.bob
        )

        assertThat(gameState).isEqualTo(States.aliceWinsCoinToss)
    }

    // 103.4
    @Test
    fun `once turn order is resolved, each player draws their starting hand`() {
        // Alice chooses to be on the draw
        val gameState = engine.performAction(
            PlayerAction.ChooseFirstPlayer(
                chosenPlayer = PlayerStateFactory.ID_BOB
            ),
            States.aliceWinsCoinToss
        )

        assertThat(gameState).isEqualTo(States.drawnFirstHands)
    }

    // 103.4
    @Test
    fun `the starting player decides whether to keep or mulligan first`() {
        // Bob decides to mulligan
        val gameState = engine.performAction(
            PlayerAction.Mulligan,
            States.drawnFirstHands
        )

        assertThat(gameState).isEqualTo(States.bobDecidedToTakeFirstMulligan)
    }

    // 103.4
    @Test
    fun `then the next player chooses whether to mulligan, after which both players will mulligan simultaneously`() {
        // Alice decides to mulligan
        val gameState = engine.performAction(
            PlayerAction.Mulligan,
            States.bobDecidedToTakeFirstMulligan
        )

        assertThat(gameState).isEqualTo(States.bothPlayersTookFirstMulligan)
    }

    // 103.4
    @Nested
    @DisplayName("this process repeats until both players decide to keep")
    inner class RepeatedMulligan {
        @Test
        fun `Bob decides to keep after first mulligan`() {
            val gameState = engine.performAction(
                PlayerAction.KeepHand(toBottom = listOf(3)), // 4th card to the bottom
                States.bothPlayersTookFirstMulligan
            )
            assertThat(gameState).isEqualTo(States.bobDecidedToKeepAfterFirstMulligan)
        }

        @Test
        fun `Alice mulligans again`() {
            val gameState2 = engine.performAction(
                PlayerAction.Mulligan,
                States.bobDecidedToKeepAfterFirstMulligan
            )
            assertThat(gameState2).isEqualTo(States.aliceTookSecondMulligan)
        }

        @Test
        fun `Alice keeps after second mulligan`() {
            val gameState2 = engine.performAction(
                PlayerAction.KeepHand(toBottom = listOf(4, 5)), // 5th and 6th card to the bottom
                States.aliceTookSecondMulligan
            )
            assertThat(gameState2).isEqualTo(States.mulligansResolved)
        }

        @Test
        fun `if Alice tries to put the wrong number of cards on the bottom it fails`() {
            assertThatThrownBy {
                engine.performAction(
                    PlayerAction.KeepHand(toBottom = listOf(1, 4, 5)),
                    States.aliceTookSecondMulligan
                )
            }.isEqualTo(
                InvalidPlayerAction(
                    action = PlayerAction.KeepHand(toBottom = listOf(1, 4, 5)),
                    state = States.aliceTookSecondMulligan,
                    reason = "toBottom should have size 2 but has size 3"
                )
            )
        }
    }
}

private val cheatShuffler = CheatShuffler<Card>(ShuffleCheat.MoveOneCardToBottom)
private fun List<Card>.shuffle(times: Int = 1) = (1..times).fold(this) { cards, _ ->
    cheatShuffler.shuffle(cards)
}

private object States {
    // Hands we expect Alice and Bob to draw due to above shuffle cheating
    val expectedAliceHand1 = DeckFactory.alice.slice(1..7)
    val expectedAliceHand2 = DeckFactory.alice.slice(2..8)
    val expectedAliceHand3 = DeckFactory.alice.slice(3..9)
    val expectedBobHand1 = DeckFactory.bob.slice(1..7)
    val expectedBobHand2 = DeckFactory.bob.slice(2..8)

    val aliceWinsCoinToss by lazy {
        GameState(
            players = listOf(
                PlayerState(
                    id = PlayerStateFactory.ID_ALICE,
                    library = DeckFactory.alice.shuffle(1),
                    lifeTotal = 20
                ),
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    library = DeckFactory.bob.shuffle(1),
                    lifeTotal = 20
                )
            ),
            gameStart = StartingPlayerMustBeChosen(PlayerStateFactory.ID_ALICE)
        )
    }

    val drawnFirstHands by lazy {
        GameState(
            players = listOf(
                PlayerState(
                    id = PlayerStateFactory.ID_ALICE,
                    library = DeckFactory.alice.shuffle(1).minus(elements = expectedAliceHand1),
                    lifeTotal = 20,
                    hand = expectedAliceHand1
                ),
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    library = DeckFactory.bob.shuffle(1).minus(elements = expectedBobHand1),
                    lifeTotal = 20,
                    hand = expectedBobHand1
                )
            ),
            gameStart = ResolvingMulligans(
                numberOfMulligans = 0,
                startingPlayer = PlayerStateFactory.ID_BOB,
                turnToDecide = PlayerStateFactory.ID_BOB,
                mulliganDecisions = mapOf(
                    PlayerStateFactory.ID_ALICE to MulliganDecision.UNDECIDED,
                    PlayerStateFactory.ID_BOB to MulliganDecision.UNDECIDED
                )
            )
        )
    }

    val bobDecidedToTakeFirstMulligan by lazy {
        GameState(
            players = listOf(
                // Alice is unchanged
                drawnFirstHands.players[0],
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    library = DeckFactory.bob.shuffle(1),
                    lifeTotal = 20,
                    hand = emptyList()
                )
            ),
            gameStart = ResolvingMulligans(
                numberOfMulligans = 0,
                startingPlayer = PlayerStateFactory.ID_BOB,
                turnToDecide = PlayerStateFactory.ID_ALICE,
                mulliganDecisions = mapOf(
                    PlayerStateFactory.ID_ALICE to MulliganDecision.UNDECIDED,
                    PlayerStateFactory.ID_BOB to MulliganDecision.MULLIGAN
                )
            )
        )
    }

    val bothPlayersTookFirstMulligan by lazy {
        GameState(
            players = listOf(
                PlayerState(
                    id = PlayerStateFactory.ID_ALICE,
                    lifeTotal = 20,
                    // Should have drawn their second hand
                    hand = expectedAliceHand2,
                    // Library should have been shuffled twice now
                    library = DeckFactory.alice.shuffle(2).minus(elements = expectedAliceHand2)
                ),
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    lifeTotal = 20,
                    hand = expectedBobHand2,
                    library = DeckFactory.bob.shuffle(2).minus(elements = expectedBobHand2)
                )
            ),
            gameStart = ResolvingMulligans(
                numberOfMulligans = 1,
                startingPlayer = PlayerStateFactory.ID_BOB,
                turnToDecide = PlayerStateFactory.ID_BOB,
                mulliganDecisions = mapOf(
                    // Both go back to undecided, since they have to decide whether to keep new hand
                    PlayerStateFactory.ID_ALICE to MulliganDecision.UNDECIDED,
                    PlayerStateFactory.ID_BOB to MulliganDecision.UNDECIDED
                )
            )
        )
    }

    val bobDecidedToKeepAfterFirstMulligan by lazy {
        GameState(
            players = listOf(
                // Alice is unchanged
                bothPlayersTookFirstMulligan.players[0],
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    lifeTotal = 20,
                    // Bob put the card at index 3 to the bottom
                    hand = expectedBobHand2.minus(expectedBobHand2[3]),
                    library = DeckFactory.bob
                        .shuffle(2)
                        .minus(expectedBobHand2)
                        .plus(expectedBobHand2[3])
                )
            ),
            gameStart = ResolvingMulligans(
                numberOfMulligans = 1,
                startingPlayer = PlayerStateFactory.ID_BOB,
                turnToDecide = PlayerStateFactory.ID_ALICE,
                mulliganDecisions = mapOf(
                    PlayerStateFactory.ID_ALICE to MulliganDecision.UNDECIDED,
                    PlayerStateFactory.ID_BOB to MulliganDecision.KEEP
                )
            )
        )
    }

    val aliceTookSecondMulligan by lazy {
        GameState(
            players = listOf(
                PlayerState(
                    id = PlayerStateFactory.ID_ALICE,
                    lifeTotal = 20,
                    // Alice draws her third hand
                    hand = expectedAliceHand3,
                    library = DeckFactory.alice.shuffle(3).minus(expectedAliceHand3)
                ),
                // Bob is unchanged
                bobDecidedToKeepAfterFirstMulligan.players[1]
            ),
            gameStart = ResolvingMulligans(
                numberOfMulligans = 2,
                startingPlayer = PlayerStateFactory.ID_BOB,
                turnToDecide = PlayerStateFactory.ID_ALICE,
                mulliganDecisions = mapOf(
                    PlayerStateFactory.ID_ALICE to MulliganDecision.UNDECIDED,
                    PlayerStateFactory.ID_BOB to MulliganDecision.KEEP
                )
            )
        )
    }

    val mulligansResolved by lazy {
        GameState(
            players = listOf(
                PlayerState(
                    id = PlayerStateFactory.ID_ALICE,
                    lifeTotal = 20,
                    // Alice put the cards at index 4 and 5 to the bottom
                    hand = expectedAliceHand3.minus(expectedAliceHand3.slice(4..5)),
                    library = DeckFactory.alice
                        .shuffle(3)
                        .minus(elements = expectedAliceHand3)
                        .plus(expectedAliceHand3.slice(4..5))
                ),
                // Bob is unchanged
                aliceTookSecondMulligan.players[1]
            ),
            gameStart = GameStart.GameStarted(startingPlayer = PlayerStateFactory.ID_BOB)
        )
    }
}
