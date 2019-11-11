package engine.model

data class GameStateView(
    val players: List<PlayerStateView>,
    val gameStart: GameStart
)