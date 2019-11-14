package engine.model

/**
 * Representation of a magic card.
 *
 * Defines an interface mainly so that [CardView.KnownCard] can implement it and have all the same properties.
 */
sealed class Card {
    data class KnownCard(
        val name: String
    ): Card() {
        override fun toString() = name
    }
    object UnknownCard: Card() {
        override fun toString() = "?"
    }
}
