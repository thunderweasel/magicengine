package engine.random

interface Randomizer {
    // Generate a number between from and to (inclusive)
    fun randomInt(from: Int, to: Int): Int
}
