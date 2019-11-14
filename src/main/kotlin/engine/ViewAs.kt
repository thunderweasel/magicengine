package engine

import engine.model.Card
import engine.model.GameState
import engine.model.PlayerId
import engine.model.PlayerState

fun GameState.viewAs(viewingAsPlayer: PlayerId): GameState =
    GameState(
        players = players.map { player ->
            PlayerState(
                id = player.id,
                lifeTotal = player.lifeTotal,
                hand = if (player.id == viewingAsPlayer) {
                    player.hand
                } else {
                    player.hand.map { Card.UnknownCard }
                },
                library = player.library.map { Card.UnknownCard }
            )
        },
        gameStart = gameStart
    )