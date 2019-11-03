package engine.shuffler

// Doesn't actually shuffle, just reverses the deck
class ReverseShuffler<T> : Shuffler<T> {
    override fun shuffle(cards: List<T>) = cards.reversed()
}