package engine.acceptance

import engine.MagicEngine
import engine.factories.DeckFactory
import engine.factories.PlayerFactory
import engine.model.GameAction
import engine.model.GameState
import engine.model.PlayerState
import engine.model.TurnOrder
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
                PlayerFactory.ID_ALICE // rig the coin toss for Alice
            )
        )
    )

    // 103.1, 103.2
    "At the start of the game, decks are shuffled and a random player gets to choose turn order" {
        val gameState = engine.start2PlayerGame(
            player1 = PlayerFactory.alice,
            player2 = PlayerFactory.bob
        )

        gameState shouldBe States.aliceWinsCoinToss
    }

    // 103.4
    "once turn order is resolved, each player draws their starting hand" {
        val gameState = engine.performAction(
            GameAction.ChooseFirstPlayer(
                actingPlayer = PlayerFactory.ID_ALICE,
                chosenPlayer = PlayerFactory.ID_BOB
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
                    id = PlayerFactory.ID_ALICE,
                    player = PlayerFactory.alice,
                    library = DeckFactory.alice.reversed()
                ),
                PlayerState(
                    id = PlayerFactory.ID_BOB,
                    player = PlayerFactory.bob,
                    library = DeckFactory.bob.reversed()
                )
            ),
            turnOrder = TurnOrder.PlayerMustChoose(playerId = PlayerFactory.ID_ALICE)
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
                    id = PlayerFactory.ID_ALICE,
                    player = PlayerFactory.alice,
                    hand = expectedAliceHand,
                    library = DeckFactory.alice.reversed().minus(expectedAliceHand)
                ),
                PlayerState(
                    id = PlayerFactory.ID_BOB,
                    player = PlayerFactory.bob,
                    hand = expectedBobHand,
                    library = DeckFactory.bob.reversed().minus(expectedBobHand)
                )
            ),
            turnOrder = TurnOrder.PlayerGoesFirst(playerId = PlayerFactory.ID_BOB)
        )
    }
}