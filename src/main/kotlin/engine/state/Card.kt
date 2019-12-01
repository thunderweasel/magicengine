package engine.state

import kotlinx.serialization.Serializable

/**
 * Representation of a magic card.
 *
 * Defines an interface mainly so that [CardView.KnownCard] can implement it and have all the same properties.
 */
@Serializable
sealed class Card {
    @Serializable
    data class KnownCard(
        val name: String
    ) : Card() {
        override fun toString() = name
    }
    @Serializable
    object UnknownCard : Card() {
        override fun toString() = "?"
    }
}
