package engine.model

/**
 * Representation of a card that may or may not be known to the player depending on the game state.
 */
sealed class CardView {
    data class KnownCard(val card: Card): CardView(), Card by card {
        override fun toString() = name
    }
    object UnknownCard: CardView() {
        override fun toString() = "UNKNOWN"
    }
}