package engine.state

import engine.cards.CardType
import kotlinx.serialization.Serializable

@Serializable
data class Permanent(
    val name: String,
    val cardTypes: List<CardType>,
    val subtypes: List<String>,
    val card: Card?
)
