package engine.model

import kotlinx.serialization.Serializable

@Serializable
sealed class GameStart
@Serializable
data class StartingPlayerMustBeChosen(val player: PlayerId?) : GameStart()
@Serializable
data class ResolvingMulligans(
    val numberOfMulligans: Int,
    val startingPlayer: PlayerId,
    val turnToDecide: PlayerId,
    val mulliganDecisions: Map<PlayerId, MulliganDecision>
) : GameStart()
@Serializable
data class GameStarted(val startingPlayer: PlayerId) : GameStart()
