package engine.state

import kotlinx.serialization.Serializable

typealias AbilityId = Long

@Serializable
data class ActivatedAbility(
    val id: AbilityId,
    val permanentId: PermanentId,
    val specId: engine.cards.AbilitySpecId
)
