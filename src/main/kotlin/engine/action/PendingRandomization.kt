package engine.action

import engine.state.RandomRequest
import kotlinx.serialization.Serializable

@Serializable
data class PendingRandomization(
    val actionOnResolution: InnerAction,
    val request: RandomRequest
)
