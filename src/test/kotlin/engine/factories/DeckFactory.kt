package engine.factories

import engine.cards.BasicLandSpec
import engine.cards.ForestSpec
import engine.cards.IslandSpec
import engine.state.Card.KnownCard

object DeckFactory {
    private const val numberOfLands = 20
    private const val deckSize = 60

    val alice by lazy {
        createBurnSpellDeck(landSpec = ForestSpec, startingId = 1)
    }
    val aliceCardNames by lazy { alice.map { it.name } }

    val bob by lazy {
        createBurnSpellDeck(landSpec = IslandSpec, startingId = 61)
    }
    val bobCardNames by lazy { bob.map { it.name } }

    private fun createBurnSpellDeck(landSpec: BasicLandSpec, startingId: Int) =
        (1..numberOfLands)
            .map {
                KnownCard(id = startingId - 1 + it, name = landSpec.name)
            }
            .plus(
                (1..(deckSize - numberOfLands))
                    .map {
                        KnownCard(id = startingId - 1 + numberOfLands + it, name = "$it")
                    }
            )
}
