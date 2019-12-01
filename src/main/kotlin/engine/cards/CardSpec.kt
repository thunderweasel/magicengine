package engine.cards

/**
 * A [CardSpec] specifies how a card actually functions. It should not be used in game state. Should be looked up by card
 * name from a [Card] instead.
 */
interface CardSpec {
    val name: String
    val cardType: CardType
    val subtype: String
    val isBasicLand: Boolean
}
