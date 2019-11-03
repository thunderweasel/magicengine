package engine.acceptance

import engine.action.GameAction
import engine.MagicEngine
import engine.factories.DeckFactory
import engine.factories.PlayerStateFactory
import engine.model.GameState
import engine.model.PlayerState
import engine.model.GameStart
import engine.shuffler.FakeRandomizer
import engine.shuffler.ReverseShuffler
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class StartingTheGameTest : StringSpec({
    val engine = MagicEngine(
        // Instead of actually shuffling, we'll just reverse the deck
        shuffler = ReverseShuffler(),
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

    // 103.4
    "once turn order is resolved, each player draws their starting hand" {
        val gameState = engine.performAction(
            GameAction.ChooseFirstPlayer(
                chosenPlayer = PlayerStateFactory.ID_BOB
            ),
            States.aliceWinsCoinToss)

        gameState shouldBe States.drawnFirstHands
    }
})

private object States {
    val aliceWinsCoinToss by lazy {
        GameState(
            players = listOf(
                PlayerState(
                    id = PlayerStateFactory.ID_ALICE,
                    library = DeckFactory.alice.reversed(),
                    lifeTotal = 20
                ),
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    library = DeckFactory.bob.reversed(),
                    lifeTotal = 20
                )
            ),
            gameStart = GameStart.PlayerMustDecideWhoGoesFirst(playerId = PlayerStateFactory.ID_ALICE)
        )
    }

    val drawnFirstHands by lazy {
        val expectedAliceHand = listOf(
            DeckFactory.alice[59],
            DeckFactory.alice[58],
            DeckFactory.alice[57],
            DeckFactory.alice[56],
            DeckFactory.alice[55],
            DeckFactory.alice[54],
            DeckFactory.alice[53]
        )
        val expectedBobHand = listOf(
            DeckFactory.bob[59],
            DeckFactory.bob[58],
            DeckFactory.bob[57],
            DeckFactory.bob[56],
            DeckFactory.bob[55],
            DeckFactory.bob[54],
            DeckFactory.bob[53]
        )
        GameState(
            players = listOf(
                PlayerState(
                    id = PlayerStateFactory.ID_ALICE,
                    library = DeckFactory.alice.reversed().minus(elements = expectedAliceHand),
                    lifeTotal = 20,
                    hand = expectedAliceHand
                ),
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    library = DeckFactory.bob.reversed().minus(elements = expectedBobHand),
                    lifeTotal = 20,
                    hand = expectedBobHand
                )
            ),
            gameStart = GameStart.Mulligans(currentPlayer = PlayerStateFactory.ID_BOB)
        )
    }
}