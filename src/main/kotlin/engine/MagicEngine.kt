package engine

import engine.shuffler.DefaultShuffler
import engine.shuffler.Shuffler

class MagicEngine(
    val shuffler: Shuffler<Card> = DefaultShuffler()
) {
    fun start2PlayerGame(player1: Player, player2: Player): GameState {
        return GameState(
            players = listOf(player1, player2).mapIndexed { index, player ->
                PlayerState(
                    id = index + 1,
                    player = player,
                    hand = shuffler.shuffle(player.deck).slice(0..6)
                )
            }
        )
    }
}
