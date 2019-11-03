package engine.domain

import engine.factories.PlayerStateFactory
import engine.model.Card
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.specs.StringSpec

class DrawCardsTest : StringSpec({
    "puts the correct number of cards in the hand and removes from the library" {
        val newPlayerState = PlayerStateFactory.create(
            library = listOf("1", "2", "3", "4", "5").map { Card(it) }
        ).drawCards(3)

        newPlayerState.hand shouldContainExactlyInAnyOrder listOf(
            Card("1"), Card("2"), Card("3")
        )
        newPlayerState.library shouldContainExactlyInAnyOrder listOf(
            Card("4"), Card("5")
        )
    }
})