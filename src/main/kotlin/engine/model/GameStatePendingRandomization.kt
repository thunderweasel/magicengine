package engine.model

import engine.action.PendingRandomization

data class GameStatePendingRandomization(
    val gameState: GameState,
    val pendingAction: PendingRandomization? = null
)

fun GameState.noPendingRandomization() =
    GameStatePendingRandomization(this, pendingAction = null)

fun GameState.pendingRandomization(pendingAction: PendingRandomization) =
    GameStatePendingRandomization(this, pendingAction = pendingAction)

fun GameState.pendingRandomization(pendingActionCreator: GameState.() -> PendingRandomization?) =
    GameStatePendingRandomization(this, pendingAction = this.pendingActionCreator())