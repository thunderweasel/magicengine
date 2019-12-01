package engine.domain

import engine.state.PlayerId
import engine.state.PlayerState

fun nextInTurnOrder(
    current: PlayerId,
    players: List<PlayerState>
): PlayerId {
    val indexOfCurrent = players.indexOfFirst { it.id == current }
    require(indexOfCurrent != -1) { "current ID $current not in players: $players" }
    return players[getNextIndex(indexOfCurrent, players)].id
}

fun nextInTurnOrder(
    current: PlayerId,
    players: List<PlayerState>,
    filter: (PlayerState) -> Boolean
): PlayerId? {
    val indexOfCurrent = players.indexOfFirst { it.id == current }
    require(indexOfCurrent != -1) { "current ID $current not in players: $players" }

    if (players.none(filter)) {
        return null
    }

    var nextIndex = getNextIndex(indexOfCurrent, players)
    while (!filter(players[nextIndex])) {
        nextIndex = getNextIndex(nextIndex, players)
    }

    return players[nextIndex].id
}

fun firstInTurnOrder(
    startingPlayer: PlayerId,
    players: List<PlayerState>,
    filter: (PlayerState) -> Boolean
): PlayerId? = if (filter(players.first { it.id == startingPlayer })) {
    startingPlayer
} else {
    nextInTurnOrder(startingPlayer, players, filter)
}

private fun getNextIndex(
    currentIndex: Int,
    players: List<PlayerState>
) = (currentIndex + 1) % players.size
