package engine

data class PlayerState(
    val id: Int,
    val player: Player,
    val hand: List<Card>
)