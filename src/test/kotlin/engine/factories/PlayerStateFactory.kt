package engine.factories

import engine.model.Card
import engine.model.PlayerState

object PlayerStateFactory {
    const val ID_ALICE = 1
    const val ID_BOB = 2

    fun createPlayerState(
        id: Int = 0,
        hand: List<Card> = listOf(),
        library: List<Card> = DeckFactory.alice,
        lifeTotal: Long = 20
    ) = PlayerState(
        id = id,
        hand = hand,
        library = library,
        lifeTotal = lifeTotal
    )
}