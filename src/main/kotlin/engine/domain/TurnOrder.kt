package engine.domain

import engine.model.PlayerId
import engine.model.PlayerState

fun nextInTurnOrder(current: PlayerId, players: List<PlayerState>): PlayerId {
    val nextIndex = (1 + players.indexOfFirst { it.id == current }) % players.size
    return players[nextIndex].id
}