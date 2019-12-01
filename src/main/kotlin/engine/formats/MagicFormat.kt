package engine.formats

import engine.cards.CardSpecLookup

interface MagicFormat {
    val name: String
    val cardLookup: CardSpecLookup
}
