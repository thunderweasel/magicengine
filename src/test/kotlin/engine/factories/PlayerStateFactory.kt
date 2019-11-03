package engine.factories

import engine.model.Card
import engine.model.MulliganDecision
import engine.model.PlayerState

object PlayerStateFactory {
    const val ID_ALICE = 1
    const val ID_BOB = 2

    fun create(
        id: Int = 0,
        hand: List<Card> = listOf(),
        library: List<Card> = DeckFactory.alice.minus(hand),
        lifeTotal: Long = 20,
        mulliganDecision: MulliganDecision = MulliganDecision.UNDECIDED
    ) = PlayerState(
        id = id,
        hand = hand,
        library = library,
        lifeTotal = lifeTotal,
        mulliganDecision = mulliganDecision
    )

    fun createAlice(
        hand: List<Card> = listOf(),
        lifeTotal: Long = 20,
        mulliganDecision: MulliganDecision = MulliganDecision.UNDECIDED
    ) = PlayerState(
        id = ID_ALICE,
        hand = hand,
        library = DeckFactory.alice.minus(hand),
        lifeTotal = lifeTotal,
        mulliganDecision = mulliganDecision
    )

    fun createBob(
        hand: List<Card> = listOf(),
        lifeTotal: Long = 20,
        mulliganDecision: MulliganDecision = MulliganDecision.UNDECIDED
    ) = PlayerState(
        id = ID_BOB,
        hand = hand,
        library = DeckFactory.bob.minus(hand),
        lifeTotal = lifeTotal,
        mulliganDecision = mulliganDecision
    )
}