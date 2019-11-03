package engine.model

data class PlayerState(
    val id: Int,
    val hand: List<Card> = listOf(),
    val library: List<Card> = listOf(),
    val lifeTotal: Int
)