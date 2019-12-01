package engine.state

import kotlinx.serialization.Serializable

/**
 * Historical information about a particular turn
 */
@Serializable
data class TurnHistory(
    val numberOfLandsPlayed: Int = 0
)
