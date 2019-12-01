package engine.reducer

import engine.action.PlayerAction
import engine.state.GameState
import engine.state.InvalidPlayerAction
import engine.state.PlayerId
import engine.state.PlayerState
import engine.state.Turn

inline fun GameState.replacePlayerState(
    id: PlayerId,
    crossinline compute: PlayerState.() -> PlayerState
) = players.map {
    if (it.id == id) {
        it.compute()
    } else {
        it
    }
}

fun GameState.player(id: PlayerId) = players.first { it.id == id }

fun List<PlayerState>.replacePlayerStates(
    playersToReplace: List<PlayerState>
) = map { existingPlayer ->
    playersToReplace.firstOrNull { it.id == existingPlayer.id } ?: existingPlayer
}

fun mustBeActivePlayerWithPriority(state: GameState, action: PlayerAction): Turn {
    mustBeActivePlayer(state, action)
    return mustHavePriority(state, action)
}

fun mustBeActivePlayer(state: GameState, action: PlayerAction): Turn {
    val turn = mustBeInTurn(state, action)
    if (action.actingPlayer != turn.activePlayer) {
        throw InvalidPlayerAction(
            action = action,
            state = state,
            reason = "Player ${action.actingPlayer} is not the active player"
        )
    }
    return turn
}

fun mustHavePriority(state: GameState, action: PlayerAction): Turn {
    val turn = mustBeInTurn(state, action)
    if (action.actingPlayer != turn.priority) {
        throw InvalidPlayerAction(
            action = action,
            state = state,
            reason = "Player ${action.actingPlayer} does not have priority"
        )
    }
    return turn
}

fun mustBeInTurn(state: GameState, action: PlayerAction): Turn {
    if (state.temporalPosition !is Turn) {
        throw InvalidPlayerAction.invalidTemporalState(
            action = action,
            state = state
        )
    }
    return state.temporalPosition
}
