package engine.random

import kotlin.random.Random

class RandomShuffler<T>(
    private val random: Random = Random.Default
): Shuffler<T> {
    override fun shuffle(cards: List<T>): List<T> = cards.shuffled(random = random)
}