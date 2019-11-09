package engine.acceptance

import engine.MagicEngine
import engine.action.PlayerAction
import engine.factories.DeckFactory
import engine.factories.PlayerStateFactory
import engine.model.Card
import engine.model.GameStart.StartingPlayerMustBeChosen
import engine.model.GameStart.ResolvingMulligans
import engine.model.GameState
import engine.model.MulliganDecision
import engine.model.PlayerState
import engine.random.FakeRandomizer
import engine.random.Shuffler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StartingTheGameTest {
    private val engine = MagicEngine(
        shuffler = cheatShuffler,
        // Feeding fake values for random choices
        randomizer = FakeRandomizer(
            listOf(
                PlayerStateFactory.ID_ALICE // rig the coin toss for Alice
            )
        )
    )

    // 103.1, 103.2, 103.3 (implicit)
    @Test
    fun `At the start of the game, decks are shuffled and a random player gets to choose turn order`() {
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
            States.aliceWinsCoinToss)

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
                PlayerAction.KeepHand,
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
    }
}

// Instead of actually shuffling, we'll just move the first card from the beginning to the end
private val cheatShuffler = object : Shuffler<Card> {
    override fun shuffle(cards: List<Card>) = cards.drop(1).plus(cards[0])
}

private fun List<Card>.shuffle(times: Int = 1): List<Card> {
    var cards = this
    for(i in 1..times) {
        cards = cheatShuffler.shuffle(cards)
    }
    return cards
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
                    lifeTotal = 20,
                    mulliganDecision = MulliganDecision.UNDECIDED
                ),
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    library = DeckFactory.bob.shuffle(1),
                    lifeTotal = 20,
                    mulliganDecision = MulliganDecision.UNDECIDED
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
                    hand = expectedAliceHand1,
                    mulliganDecision = MulliganDecision.UNDECIDED
                ),
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    library = DeckFactory.bob.shuffle(1).minus(elements = expectedBobHand1),
                    lifeTotal = 20,
                    hand = expectedBobHand1,
                    mulliganDecision = MulliganDecision.UNDECIDED
                )
            ),
            gameStart = ResolvingMulligans(
                startingPlayer = PlayerStateFactory.ID_BOB,
                currentChoice = PlayerStateFactory.ID_BOB
            )
        )
    }

    val bobDecidedToTakeFirstMulligan by lazy {
        GameState(
            players = listOf(
                PlayerState(
                    id = PlayerStateFactory.ID_ALICE,
                    library = DeckFactory.alice.shuffle(1).minus(elements = expectedAliceHand1),
                    lifeTotal = 20,
                    hand = expectedAliceHand1,
                    mulliganDecision = MulliganDecision.UNDECIDED
                ),
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    library = DeckFactory.bob.shuffle(1),
                    lifeTotal = 20,
                    hand = emptyList(),
                    mulliganDecision = MulliganDecision.WILL_MULLIGAN
                )
            ),
            gameStart = ResolvingMulligans(
                startingPlayer = PlayerStateFactory.ID_BOB,
                currentChoice = PlayerStateFactory.ID_ALICE
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
                    library = DeckFactory.alice.shuffle(2).minus(elements = expectedAliceHand2),
                    // Goes back to undecided, since she has to decide whether to keep the new hand
                    mulliganDecision = MulliganDecision.UNDECIDED
                ),
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    lifeTotal = 20,
                    hand = expectedBobHand2,
                    library = DeckFactory.bob.shuffle(2).minus(elements = expectedBobHand2),
                    mulliganDecision = MulliganDecision.UNDECIDED
                )
            ),
            gameStart = ResolvingMulligans(
                startingPlayer = PlayerStateFactory.ID_BOB,
                currentChoice = PlayerStateFactory.ID_BOB
            )
        )
    }

    val bobDecidedToKeepAfterFirstMulligan by lazy {
        GameState(
            players = listOf(
                PlayerState(
                    id = PlayerStateFactory.ID_ALICE,
                    lifeTotal = 20,
                    hand = expectedAliceHand2,
                    library = DeckFactory.alice.shuffle(2).minus(elements = expectedAliceHand2),
                    mulliganDecision = MulliganDecision.UNDECIDED
                ),
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    lifeTotal = 20,
                    // Bob should still have same hand, since he kept
                    hand = expectedBobHand2,
                    library = DeckFactory.bob.shuffle(2).minus(elements = expectedBobHand2),
                    mulliganDecision = MulliganDecision.WILL_KEEP
                )
            ),
            gameStart = ResolvingMulligans(
                startingPlayer = PlayerStateFactory.ID_BOB,
                currentChoice = PlayerStateFactory.ID_ALICE
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
                    library = DeckFactory.alice.shuffle(3).minus(elements = expectedAliceHand3),
                    mulliganDecision = MulliganDecision.UNDECIDED
                ),
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    lifeTotal = 20,
                    hand = expectedBobHand2,
                    library = DeckFactory.bob.shuffle(2).minus(elements = expectedBobHand2),
                    mulliganDecision = MulliganDecision.WILL_KEEP
                )
            ),
            gameStart = ResolvingMulligans(
                startingPlayer = PlayerStateFactory.ID_BOB,
                currentChoice = PlayerStateFactory.ID_ALICE
            )
        )
    }
}