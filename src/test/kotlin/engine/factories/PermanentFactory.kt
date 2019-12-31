package engine.factories

import engine.cards.BasicLandSpec
import engine.cards.CardType
import engine.state.ActivatedAbility
import engine.state.Card
import engine.state.Permanent
import engine.state.PermanentId
import engine.state.PlayerId

object PermanentFactory {
    fun createBasicLand(spec: BasicLandSpec, permanentId: PermanentId, owner: PlayerId) =
        Permanent(
            id = permanentId,
            name = spec.name,
            cardTypes = listOf(CardType.LAND),
            subtypes = listOf(spec.name),
            card = Card.KnownCard(
                id = 0,
                name = spec.name
            ),
            activatedAbilities = listOf(
                ActivatedAbility(
                    id = 1,
                    permanentId = 1,
                    specId = spec.manaAbility.id
                )
            ),
            controller = owner
        )
}
