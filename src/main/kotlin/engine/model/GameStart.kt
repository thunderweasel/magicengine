package engine.model

sealed class GameStart {
    data class PlayerMustDecideWhoGoesFirst(val playerId: PlayerId) : GameStart()
    data class Mulligans(
        val currentPlayer: PlayerId,
        val mulliganStates: Map<PlayerId, MulliganDecision>
    ) : GameStart()
}
