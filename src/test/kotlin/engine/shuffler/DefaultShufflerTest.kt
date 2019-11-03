package engine.shuffler

import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import kotlin.random.Random

class DefaultShufflerTest : StringSpec({
    val cards = listOf(
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10
    )
    "it has all the same cards in it when it's done" {
        DefaultShuffler<Int>().shuffle(cards) shouldContainExactlyInAnyOrder cards
    }

    "it always has the same result if the seed is the same" {
        // If this fails, then either the algorithm has changed, or the seed isn't being respected
        DefaultShuffler<Int>(
            random = Random(seed = 42)
        ).shuffle(cards) shouldBe listOf(
            6,
            1,
            5,
            10,
            3,
            9,
            2,
            8,
            7,
            4
        )
    }
})