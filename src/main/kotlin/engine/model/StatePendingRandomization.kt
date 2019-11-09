package engine.model

import engine.action.PendingRandomization

data class StatePendingRandomization<T>(
    val gameState: T,
    val pendingAction: PendingRandomization? = null
)

typealias GameStatePendingRandomization = StatePendingRandomization<GameState>

fun GameState.noPendingRandomization() =
    StatePendingRandomization(this, pendingAction = null)

fun GameState.pendingRandomization(pendingAction: PendingRandomization) =
    StatePendingRandomization(this, pendingAction = pendingAction)

fun GameState.pendingRandomization(pendingActionCreator: GameState.() -> PendingRandomization?) =
    StatePendingRandomization(this, pendingAction = pendingActionCreator())