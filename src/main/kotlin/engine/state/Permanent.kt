package engine.state

import engine.cards.CardType
import kotlinx.serialization.Serializable

typealias PermanentId = Long

@Serializable
data class Permanent(
    val id: PermanentId,
    val name: String,
    val cardTypes: List<CardType>,
    val subtypes: List<String>,
    val card: Card?,
    val activatedAbilities: List<ActivatedAbility> = emptyList(),
    val tapped: Boolean = false,
    val controller: PlayerId
)
