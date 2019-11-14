package engine.model

/**
 * Representation of the entire game state.
 */
data class GameState(
    val viewer: PlayerId? = null,
    val players: List<PlayerState>,
    val gameStart: GameStart
)