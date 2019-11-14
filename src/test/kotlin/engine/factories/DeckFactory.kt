package engine.factories

import engine.model.Card.KnownCard

object DeckFactory {
    val alice by lazy {
        nonsenseDeck("A")
    }
    val bob by lazy {
        nonsenseDeck("B")
    }

    fun nonsenseDeck(prefix: String) =
        (0..59).toList().map {
            KnownCard("$prefix$it")
        }
}