package engine.model

/**
 * Representation of the entire game state.
 *
 * [GameStateGeneric] allows the type of the player state to be swapped out for use in [GameStateView], but normally
 * when using [GameState] this is just [PlayerState].
 */
data class GameStateGeneric<PLAYER_STATE : Any>(
    val players: List<PLAYER_STATE>,
    val gameStart: GameStart
)
typealias GameState = GameStateGeneric<PlayerState>