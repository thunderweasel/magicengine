package engine.model

data class RandomRequest(
    val shuffles: List<List<Card>> = emptyList(),
    val randomNumbers: List<IntRange> = emptyList()
)