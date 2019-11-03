package engine.factories

import engine.model.Card

object DeckFactory {
    val alice by lazy {
        nonsenseDeck("alice")
    }
    val bob by lazy {
        nonsenseDeck("bob")
    }

    val aliceExampleHand = listOf(
        alice[0],
        alice[2],
        alice[4],
        alice[6],
        alice[8],
        alice[10],
        alice[12]
    )
    val bobExampleHand = listOf(
        bob[3],
        bob[4],
        bob[5],
        bob[6],
        bob[7],
        bob[8],
        bob[9]
    )

    fun nonsenseDeck(prefix: String) =
        (0..59).toList().map {
            Card("$prefix $it")
        }
}