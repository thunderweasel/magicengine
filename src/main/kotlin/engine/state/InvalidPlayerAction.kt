package engine.state

import engine.action.PlayerAction

data class InvalidPlayerAction(
    val action: PlayerAction,
    val state: GameState,
    val reason: String
) : Throwable()
