package engine.action

import engine.model.RandomRequest

data class PendingRandomizedAction(
    val action: PlayerAction,
    val pendingRandomization: List<RandomRequest<*>>
)