package engine.state

import engine.cards.CardType
import kotlinx.serialization.Serializable

@Serializable
data class Permanent(
    val name: String,
    val cardType: CardType,
    val subtype: String,
    val card: Card
)
