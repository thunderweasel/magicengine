package engine.factories

import engine.Player

object PlayerFactory {
    val alice: Player by lazy {
        Player(
            name = "Alice",
            deck = DeckFactory.alice
        )
    }
    val bob: Player by lazy {
        Player(
            name = "Bob",
            deck = DeckFactory.bob
        )
    }
}