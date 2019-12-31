package engine.state

import kotlinx.serialization.Serializable

/**
 * Representation of a magic card in the game state. Does not describe how that card works in the game (see [CardSpec])
 */
@Serializable
sealed class Card {
    /**
     * The ID uniquely identifies a card within a particular game. It should stay the same across game state changes
     * unless a known card changes to an unknown card (e.g. when a card is returned to the opponent's hand from the
     * battlefield.)
     */
    abstract val id: CardId

    /**
     * A card that is known according to the current viewer. (If there is no viewer, all cards are known.)
     */
    @Serializable
    data class KnownCard(
        override val id: CardId,
        val name: String
    ) : Card() {
        override fun toString() = "$id($name)"
    }

    /**
     * A card that is not known by the current viewer.
     */
    @Serializable
    data class UnknownCard(
        override val id: CardId
    ) : Card() {
        override fun toString() = "$id(?)"
    }
}
