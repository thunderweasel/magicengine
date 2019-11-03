package engine.factories

import engine.model.Player

object PlayerFactory {
    val ID_ALICE = 1
    val ID_BOB = 2

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