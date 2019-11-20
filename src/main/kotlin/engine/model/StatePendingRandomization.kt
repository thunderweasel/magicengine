package engine.model

import engine.action.PendingRandomization
import kotlinx.serialization.Serializable

@Serializable
data class StatePendingRandomization<T>(
    val gameState: T,
    val pendingAction: PendingRandomization? = null
)

typealias GameStatePendingRandomization = StatePendingRandomization<GameState>

fun <T> T.noPendingRandomization() =
    StatePendingRandomization(this, pendingAction = null)

fun <T> T.pendingRandomization(pendingAction: PendingRandomization?) =
    StatePendingRandomization(this, pendingAction = pendingAction)

fun <T> T.pendingRandomization(pendingActionCreator: T.() -> PendingRandomization?) =
    StatePendingRandomization(this, pendingAction = pendingActionCreator())
