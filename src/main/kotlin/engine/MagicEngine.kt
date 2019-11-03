package engine

import engine.shuffler.DefaultShuffler
import engine.shuffler.Shuffler

class MagicEngine(
    val shuffler: Shuffler<Card> = DefaultShuffler()
) {
    fun start2PlayerGame(player1: Player, player2: Player): GameState {
        return GameState(
            players = setOf(
                PlayerState(
                    id = 1,
                    player = player1,
                    hand = shuffler.shuffle(player1.deck.toList()).slice(0..6).toSet()
                ),
                PlayerState(
                    id = 2,
                    player = player2,
                    hand = shuffler.shuffle(player2.deck.toList()).slice(0..6).toSet()
                )
            )
        )
    }
}
