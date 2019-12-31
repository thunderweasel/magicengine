package engine.factories

import engine.cards.BasicLandSpec
import engine.cards.CardType
import engine.formats.AllSpellsAreBurnSpells
import engine.formats.MagicFormat
import engine.state.ActivatedAbility
import engine.state.Card
import engine.state.Permanent
import engine.state.PermanentId
import engine.state.PlayerId

class PermanentFactory(
    private val format: MagicFormat = AllSpellsAreBurnSpells()
) {
    fun createBasicLand(card: Card.KnownCard, permanentId: PermanentId, controller: PlayerId): Permanent {
        val spec = format.cardLookup[card.name] as BasicLandSpec
        return Permanent(
            id = permanentId,
            name = spec.name,
            cardTypes = listOf(CardType.LAND),
            subtypes = listOf(spec.name),
            card = card,
            activatedAbilities = listOf(
                ActivatedAbility(
                    id = 1,
                    permanentId = 1,
                    specId = spec.manaAbility.id
                )
            ),
            controller = controller
        )
    }
}
