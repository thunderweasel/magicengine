package engine.cards

// TODO: Should this belong in the magic engine or in a separate module?
data class BasicLand(
    override val name: String,
    override val subtypes: List<String> = listOf(name)
) : CardSpec {
    override val isBasicLand = true
    override val cardTypes = listOf(CardType.LAND)
}
