package engine.random

interface Shuffler<T> {
    fun shuffle(cards: List<T>): List<T>
}