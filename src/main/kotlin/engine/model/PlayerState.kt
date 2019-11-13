package engine.model

/**
 * Representation of a player's state in a game.
 *
 * The type of card is just [Card] in [PlayerState], but [PlayerStateGeneric] can be used to have a state with a different
 * type of card. This is used by [PlayerStateView] to create a version of player state where all cards are represented
 * by a [CardView], which can be known or unknown depending on the game state.
 */
data class PlayerStateGeneric<CARD : Any>(
    val id: PlayerId,
    val hand: List<CARD> = listOf(),
    val library: List<CARD> = listOf(),
    val lifeTotal: Long
)
typealias PlayerState = PlayerStateGeneric<Card>