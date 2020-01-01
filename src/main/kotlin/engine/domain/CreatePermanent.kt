package engine.domain

import engine.cards.CardSpec
import engine.state.ActivatedAbility
import engine.state.Card
import engine.state.GameState
import engine.state.Permanent
import engine.state.PlayerId
import engine.state.adding

fun GameState.createPermanent(
    card: Card.KnownCard,
    cardSpec: CardSpec,
    controller: PlayerId
): GameState {
    val permanentId = idState.nextPermanentId
    return copy(
        battlefield = battlefield.adding(
            Permanent(
                id = permanentId,
                name = card.name,
                cardTypes = cardSpec.cardTypes,
                subtypes = cardSpec.subtypes,
                card = card,
                controller = controller,
                activatedAbilities = cardSpec.activatedAbilities.mapIndexed { index, abilitySpec ->
                    ActivatedAbility(
                        id = index + 1,
                        permanentId = permanentId,
                        specId = abilitySpec.id
                    )
                }
            )
        ),
        idState = idState.copy(
            nextPermanentId = permanentId + 1
        )
    )
}
