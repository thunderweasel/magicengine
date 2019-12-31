package engine.formats

import engine.cards.CardSpec
import engine.cards.CardSpecLookup
import engine.cards.ForestSpec
import engine.cards.IslandSpec
import engine.cards.MountainSpec
import engine.cards.PlainsSpec
import engine.cards.SwampSpec

class BasicLandLookup : CardSpecLookup {
    override fun get(name: String): CardSpec? = when (name) {
        PlainsSpec.name -> PlainsSpec
        IslandSpec.name -> IslandSpec
        SwampSpec.name -> SwampSpec
        MountainSpec.name -> MountainSpec
        ForestSpec.name -> ForestSpec
        else -> null
    }
}
