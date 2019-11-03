package engine.action

import engine.model.RandomRequest

data class PendingRandomizedAction(
    val playerAction: PlayerAction,
    val pendingRandomization: List<RandomRequest<*>>
)