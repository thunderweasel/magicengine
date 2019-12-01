package engine.state

import engine.action.PlayerAction

data class InvalidPlayerAction(
    val action: PlayerAction,
    val state: GameState,
    val reason: String
) : Throwable() {
    companion object {
        fun invalidTemporalState(
            action: PlayerAction,
            state: GameState
        ) = InvalidPlayerAction(
            action = action,
            state = state,
            reason = "Invalid temporal state for this action"
        )
    }
}
