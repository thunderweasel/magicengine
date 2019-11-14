package engine.model

/**
 * Representation of a magic card.
 *
 * Defines an interface mainly so that [CardView.KnownCard] can implement it and have all the same properties.
 */
sealed class Card {
    abstract val id: CardId

    data class KnownCard(
        val name: String,
        override val id: CardId
    ): Card() {
        override fun toString() = "$name($id)"
    }
    data class UnknownCard(override val id: CardId): Card() {
        override fun toString() = "?"
    }
}
