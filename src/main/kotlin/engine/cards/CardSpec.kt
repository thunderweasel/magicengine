package engine.cards

/**
 * A [CardSpec] specifies how a card actually functions. It should not be used in game state. Should be looked up by card
 * name from a [Card] instead.
 */
interface CardSpec {
    val name: String
    val cardTypes: List<CardType>
    val subtypes: List<String>
    val isBasicLand: Boolean
    val activatedAbilities: List<ActivatedAbilitySpec>
}
