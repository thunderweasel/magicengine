package engine.acceptance

import engine.action.PlayerAction
import engine.MagicEngine
import engine.factories.DeckFactory
import engine.factories.PlayerStateFactory
import engine.model.*
import engine.model.GameStart.FirstPlayerMustBeChosenBy
import engine.model.GameStart.ResolvingMulligans
import engine.random.FakeRandomizer
import engine.random.Shuffler
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class StartingTheGameTest : StringSpec({
    val engine = MagicEngine(
        shuffler = cheatShuffler,
        // Feeding fake values for random choices
        randomizer = FakeRandomizer(
            listOf(
                PlayerStateFactory.ID_ALICE // rig the coin toss for Alice
            )
        )
    )

    // 103.1, 103.2, 103.3 (implicit)
    "At the start of the game, decks are shuffled and a random player gets to choose turn order" {
        val gameState = engine.start2PlayerGame(
            deck1 = DeckFactory.alice,
            deck2 = DeckFactory.bob
        )

        gameState shouldBe States.aliceWinsCoinToss
    }

    // 103.4 (part 1)
    "once turn order is resolved, each player draws their starting hand" {
        // Alice chooses to be on the draw
        val gameState = engine.performAction(
            PlayerAction.ChooseFirstPlayer(
                chosenPlayer = PlayerStateFactory.ID_BOB
            ),
            States.aliceWinsCoinToss)

        gameState shouldBe States.drawnFirstHands
    }

    // 103.4 (part 2)
    "the starting player decides whether to keep or mulligan first" {
        // Bob decides to keep
        val gameState = engine.performAction(
            PlayerAction.KeepHand,
            States.drawnFirstHands
        )

        gameState shouldBe States.bobDecidedToKeep
    }

    // 103.4 (part 3)
    "then the next player chooses whether to mulligan, after which both players will mulligan simultaneously (if desired)" {
        // Alice decides to mulligan
        val gameState = engine.performAction(
            PlayerAction.Mulligan,
            States.bobDecidedToKeep
        )

        gameState shouldBe States.aliceDecidedToMulligan1
    }
})

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
    val expectedBobHand1 = DeckFactory.bob.slice(1..7)

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
            gameStart = FirstPlayerMustBeChosenBy(PlayerStateFactory.ID_ALICE)
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
            gameStart = ResolvingMulligans(currentChoice = PlayerStateFactory.ID_BOB)
        )
    }

    val bobDecidedToKeep by lazy {
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
                    mulliganDecision = MulliganDecision.WILL_KEEP
                )
            ),
            gameStart = ResolvingMulligans(currentChoice = PlayerStateFactory.ID_ALICE)
        )
    }

    val aliceDecidedToMulligan1 by lazy {
        GameState(
            players = listOf(
                PlayerState(
                    id = PlayerStateFactory.ID_ALICE,
                    lifeTotal = 20,
                    // Alice should have drawn their second hand
                    hand = expectedAliceHand2,
                    // Library should have been shuffled twice now
                    library = DeckFactory.alice.shuffle(2).minus(elements = expectedAliceHand2),
                    // Alice goes back to undecided, since she has to decide whether to keep the new hand
                    mulliganDecision = MulliganDecision.UNDECIDED
                ),
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    // Bob decided to keep, so he has the same hand as before
                    hand = expectedBobHand1,
                    // Library should have been only shuffled once
                    library = DeckFactory.bob.shuffle(1).minus(elements = expectedBobHand1),
                    lifeTotal = 20,
                    mulliganDecision = MulliganDecision.WILL_KEEP
                )
            ),
            gameStart = ResolvingMulligans(currentChoice = PlayerStateFactory.ID_ALICE)
        )
    }
}