package engine.state

import kotlinx.serialization.Serializable

@Serializable
data class IdState(
    val nextCardId: CardId,
    val nextPermanentId: PermanentId
)
