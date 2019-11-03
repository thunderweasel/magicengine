package engine.model

data class PlayerState(
    val id: Int,
    val player: Player,
    val hand: List<Card> = listOf(),
    val library: List<Card> = listOf()
)