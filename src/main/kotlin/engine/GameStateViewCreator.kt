package engine

import engine.model.CardView
import engine.model.GameState
import engine.model.GameStateView
import engine.model.PlayerId
import engine.model.PlayerStateView

fun GameState.viewAs(viewingAsPlayer: PlayerId): GameStateView =
    GameStateView(
        players = players.map { player ->
            PlayerStateView(
                id = player.id,
                lifeTotal = player.lifeTotal,
                hand = if (player.id == viewingAsPlayer) {
                    player.hand.map { CardView.KnownCard(it) }
                } else {
                    player.hand.map { CardView.UnknownCard }
                },
                library = player.library.map { CardView.UnknownCard }
            )
        },
        gameStart = gameStart
    )