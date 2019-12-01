package engine.action

import engine.state.Card

data class ResolvedRandomization(
    val completedShuffles: List<List<Card>> = emptyList(),
    val generatedNumbers: List<Int> = emptyList()
)
