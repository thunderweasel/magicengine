package engine

import engine.state.Card
import engine.state.GameState
import engine.state.PlayerId
import engine.state.PlayerState

fun GameState.viewAs(viewer: PlayerId): GameState =
    copy(
        viewer = viewer,
        players = players.map { player ->
            PlayerState(
                id = player.id,
                lifeTotal = player.lifeTotal,
                hand = if (player.id == viewer) {
                    player.hand
                } else {
                    player.hand.map { Card.UnknownCard(it.id) }
                },
                library = player.library.map { Card.UnknownCard(it.id) }
            )
        },
        temporalPosition = temporalPosition
    )
