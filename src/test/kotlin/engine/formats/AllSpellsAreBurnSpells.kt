package engine.formats

import engine.cards.ActivatedAbilitySpec
import engine.cards.CardSpec
import engine.cards.CardSpecLookup
import engine.cards.CardType

/**
 * All spells are sorcery cards with a numeric name. Each card's numeric name is also its generic mana cost, as well as
 * the amount of damage it does to target player on resolution
 */
class AllSpellsAreBurnSpells() : MagicFormat {
    override val name = "All Spells are Burn Spells"
    private val basicLandLookup = BasicLandLookup()
    override val cardLookup = object : CardSpecLookup {
        override fun get(name: String) = basicLandLookup[name] ?: lookupSpell(name)
        private fun lookupSpell(name: String) = name.toIntOrNull()?.let {
            object : CardSpec {
                override val name = name
                override val cardTypes = listOf(CardType.SORCERY)
                override val subtypes = emptyList<String>()
                override val isBasicLand = false
                override val activatedAbilities = emptyList<ActivatedAbilitySpec>()
            }
        }
    }
}
