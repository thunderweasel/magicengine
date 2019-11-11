package engine.model

/**
 * Representation of a magic card.
 *
 * Defines an interface mainly so that [CardView.KnownCard] can implement it and have all the same properties.
 */
interface Card {
    val name: String
}
private data class CardImpl(override val name: String) : Card {
    override fun toString() = name
}
fun card(name: String): Card = CardImpl(name)