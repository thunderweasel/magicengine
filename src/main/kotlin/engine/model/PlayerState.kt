package engine.model

/**
 * Representation of a player's state in a game.
 *
 * The type of card can be swapped out here. For example [PlayerStateView] uses type [CardView] in order to
 * allow cards to be hidden in the [GameStateView].
 */
data class PlayerStateGeneric<CARD_TYPE : Any>(
    val id: PlayerId,
    val hand: List<CARD_TYPE> = listOf(),
    val library: List<CARD_TYPE> = listOf(),
    val lifeTotal: Long
)
typealias PlayerState = PlayerStateGeneric<Card>