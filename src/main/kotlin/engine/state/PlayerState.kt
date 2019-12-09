package engine.state

import kotlinx.serialization.Serializable

/**
 * Representation of a player's state in a game.
 */
@Serializable
data class PlayerState(
    val id: PlayerId,
    val hand: List<Card> = listOf(),
    val library: List<Card> = listOf(),
    val manaPool: ManaPool = createManaPool(),
    val lifeTotal: Long
)
