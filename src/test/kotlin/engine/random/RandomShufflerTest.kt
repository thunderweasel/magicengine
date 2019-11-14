package engine.random

import kotlin.random.Random
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RandomShufflerTest {
    private val cards = listOf(
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10
    )

    @Test
    fun `it has all the same cards in it when it's done`() {
        assertThat(RandomShuffler<Int>(Random(42)).shuffle(cards)).containsExactlyInAnyOrderElementsOf(cards)
    }

    @Test
    fun `it always has the same result if the seed is the same`() {
        // If this fails, then either the algorithm has changed, or the seed isn't being respected
        assertThat(RandomShuffler<Int>(
            random = Random(seed = 42)
        ).shuffle(cards)).isEqualTo(listOf(
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
        ))
    }
}
