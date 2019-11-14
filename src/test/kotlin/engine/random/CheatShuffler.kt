package engine.random

/**
 * [Shuffler] that does various cheats instead of actually shuffling the deck.
 * Useful for testing purposes.
 */
class CheatShuffler<T>(
    private val cheat: ShuffleCheat
) : Shuffler<T> {
    override fun shuffle(cards: List<T>): List<T> =
        when (cheat) {
            ShuffleCheat.MoveOneCardToBottom -> cards.drop(1).plus(cards[0])
        }
}

sealed class ShuffleCheat {
    // Shifts one card to bottom instead of shuffling
    object MoveOneCardToBottom : ShuffleCheat()
    // Add more cheats here as needed
}
