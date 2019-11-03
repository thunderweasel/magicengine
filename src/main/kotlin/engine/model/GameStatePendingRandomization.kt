package engine.model

import engine.action.PendingRandomizedAction

data class GameStatePendingRandomization(
    val gameState: GameState,
    val pendingAction: PendingRandomizedAction? = null
)

fun GameState.noRandomization() =
    GameStatePendingRandomization(this, pendingAction = null)

fun GameState.pendingRandomization(pendingAction: PendingRandomizedAction) =
    GameStatePendingRandomization(this, pendingAction = pendingAction)

fun GameState.pendingRandomization(pendingActionCreator: GameState.() -> PendingRandomizedAction?) =
    GameStatePendingRandomization(this, pendingAction = this.pendingActionCreator())