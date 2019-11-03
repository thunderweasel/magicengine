package engine.model

data class GameState(
    val players: List<PlayerState>,
    val gameStart: GameStart
)