package engine.action

import engine.model.RandomRequest
import kotlinx.serialization.Serializable

@Serializable
data class PendingRandomization(
    val actionOnResolution: InnerAction,
    val request: RandomRequest
)
