package engine.shuffler

interface Shuffler<T> {
    fun shuffle(cards: List<T>): List<T>
}