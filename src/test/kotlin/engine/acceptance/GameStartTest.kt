package engine.acceptance

import engine.GameState
import engine.MagicEngine
import engine.PlayerState
import engine.factories.DeckFactory
import engine.factories.PlayerFactory
import engine.shuffler.CheatShuffleToTop
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class GameStartTest : StringSpec({
    "at the start of a 2 player game, each player shuffles their deck and draws 7 cards" {
        val gameState = MagicEngine(
            // cheat the shuffle order to make the test repeatable
            shuffler = CheatShuffleToTop(
                listOf(
                    DeckFactory.aliceExampleHand,
                    DeckFactory.bobExampleHand
                ).flatten()
            )
        ).start2PlayerGame(
            player1 = PlayerFactory.alice,
            player2 = PlayerFactory.bob
        )

        gameState shouldBe GameState(
            players = listOf(
                PlayerState(
                    id = 1,
                    player = PlayerFactory.alice,
                    hand = DeckFactory.aliceExampleHand
                ),
                PlayerState(
                    id = 2,
                    player = PlayerFactory.bob,
                    hand = DeckFactory.bobExampleHand
                )
            )
        )
    }
})