package engine.cards

import engine.state.ActivatedAbility
import engine.state.Card

fun CardSpecLookup.getActivatedAbility(id: AbilitySpecId): ActivatedAbilitySpec? =
    this[id.cardName]?.activatedAbilities?.find { it.id == id }

fun Card.KnownCard.lookupSpec(lookup: CardSpecLookup): CardSpec? = lookup[name]

fun ActivatedAbility.lookupSpec(lookup: CardSpecLookup): ActivatedAbilitySpec {
    return lookup.getActivatedAbility(specId)!! // Card and ability must exist if the ability exists on the battlefield
}
