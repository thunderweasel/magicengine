package engine.state

import kotlinx.serialization.Serializable

@Serializable
sealed class TemporalPosition
@Serializable
data class StartingPlayerMustBeChosen(val player: PlayerId?) : TemporalPosition()
@Serializable
data class ResolvingMulligans(
    val numberOfMulligans: Int,
    val startingPlayer: PlayerId,
    val turnToDecide: PlayerId,
    val mulliganDecisions: Map<PlayerId, MulliganDecision>
) : TemporalPosition()

@Serializable
data class Turn(
    val activePlayer: PlayerId,
    val phase: TurnPhase,
    val priority: PlayerId?,
    val firstTurn: Boolean = false,
    val history: TurnHistory = TurnHistory()
) : TemporalPosition()
