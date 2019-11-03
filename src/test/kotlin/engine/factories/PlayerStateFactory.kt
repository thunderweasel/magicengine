package engine.factories

import engine.model.Card
import engine.model.PlayerState

object PlayerStateFactory {
    fun createPlayerState(
        id: Int = 0,
        hand: List<Card> = listOf(),
        library: List<Card> = DeckFactory.alice,
        lifeTotal: Int = 20
    ): PlayerState {
        return PlayerState(
            id = id,
            hand = hand,
            library = library,
            lifeTotal = lifeTotal
        )
    }
}