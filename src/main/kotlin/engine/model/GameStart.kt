package engine.model

sealed class GameStart {
    data class StartingPlayerMustBeChosen(val player: PlayerId?) : GameStart()
    data class ResolvingMulligans(
        val numberOfMulligans: Int,
        val startingPlayer: PlayerId,
        val turnToDecide: PlayerId,
        val mulliganDecisions: Map<PlayerId, MulliganDecision>
    ) : GameStart()
    data class GameStarted(val startingPlayer: PlayerId) : GameStart()
}
