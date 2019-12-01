package engine.cards

// TODO: Should this belong in the magic engine or in a separate module?
data class BasicLand(
    override val name: String,
    override val subtype: String = name
) : CardSpec {
    override val isBasicLand = true
    override val cardType = CardType.LAND
}
