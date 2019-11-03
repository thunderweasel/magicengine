package engine

import engine.model.Card
import engine.shuffler.DefaultShuffler
import engine.shuffler.Shuffler
import engine.model.GameState
import engine.model.Player
import engine.model.PlayerState

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
