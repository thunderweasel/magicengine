package engine.formats

import engine.cards.BasicLand
import engine.cards.CardSpecLookup

/**
 * The most riveting format in Magic, where there are no invalid card names, and everything is a Forest.
 */
class EverythingIsAForest : MagicFormat {
    override val name: String = "Everything is a Forest"
    override val cardLookup: CardSpecLookup = object : CardSpecLookup {
        override fun get(name: String) = BasicLand(
            name = name,
            subtype = "Forest"
        )
    }
}
