package engine.random

import kotlin.random.Random

class ActualRandomizer(
    private val random: Random
) : Randomizer {
    override fun randomInt(from: Int, to: Int): Int = random.nextInt(from, to + 1)
}
