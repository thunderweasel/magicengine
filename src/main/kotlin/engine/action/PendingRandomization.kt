package engine.action

import engine.model.RandomRequest

data class PendingRandomization(
    val actionOnResolution: RandomizedResultAction.InnerAction,
    val request: RandomRequest
)
