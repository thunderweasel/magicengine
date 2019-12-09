package engine.cards

import kotlinx.serialization.Serializable

@Serializable
data class AbilitySpecId(
    val cardName: String,
    val number: Int
)
