package engine.action

data class ResolvedRandomization(
    // Each completed shuffle will have a list of integers, where each integer is the original position of that
    // card in the list before it was shuffled.
    val completedShuffles: List<List<Int>> = emptyList(),
    val generatedNumbers: List<Int> = emptyList()
)
