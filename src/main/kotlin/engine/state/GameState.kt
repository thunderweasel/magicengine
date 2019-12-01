package engine.state

import kotlinx.serialization.Serializable

/**
 * Representation of the entire game state.
 */
@Serializable
data class GameState(
    val viewer: PlayerId? = null,
    val players: List<PlayerState>,
    val battlefield: List<Permanent> = emptyList(),
    val temporalPosition: TemporalPosition
)
