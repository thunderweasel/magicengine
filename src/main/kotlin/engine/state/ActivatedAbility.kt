package engine.state

import engine.cards.AbilitySpecId
import kotlinx.serialization.Serializable

typealias AbilityId = Long

/**
 * Representation of an activated ability on a permanent in the game state.
 * @param id Uniquely identifies an ability within the permanent itself. Must be combined with [permanentId] to be
 * globally unique within the game state.
 * @param permanentId The ID for the permanent in which the ability resides.
 * @param specId The ID for the [ActivatedAbilitySpec] that describes how the ability works.
 */
@Serializable
data class ActivatedAbility(
    val id: AbilityId,
    val permanentId: PermanentId,
    val specId: AbilitySpecId
)
