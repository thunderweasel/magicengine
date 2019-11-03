package engine.shuffler

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class CheatShuffleToTopTest : StringSpec({
    val cards = listOf(
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10
    )

    "cheats the cards to the top" {
        CheatShuffleToTop(
            cardsToTop = listOf(5, 2, 7)
        ).shuffle(cards) shouldBe listOf(5, 2, 7, 1, 3, 4, 6, 8, 9, 10)
    }

    "non-existent cards are ignored" {
        CheatShuffleToTop(
            cardsToTop = listOf(5, 2, 7, 13, 57)
        ).shuffle(cards) shouldBe listOf(5, 2, 7, 1, 3, 4, 6, 8, 9, 10)
    }
})