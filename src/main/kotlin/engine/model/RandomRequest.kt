package engine.model

sealed class RandomRequest<RESULT_TYPE> {
    data class Shuffle(val cards: List<Card>): RandomRequest<List<Card>>()
    data class NumberInRange(val range: IntRange): RandomRequest<Int>()
}