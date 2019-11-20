package engine.action

import engine.model.RandomRequest

data class PendingRandomization(
    val actionOnResolution: InnerAction,
    val request: RandomRequest
)
