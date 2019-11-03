package engine.model

import engine.domain.MulliganState
import engine.domain.PlayerId

sealed class GameStart {
    data class PlayerMustDecideWhoGoesFirst(val playerId: PlayerId) : GameStart()
    data class Mulligans(
        val currentPlayer: PlayerId,
        val mulliganStates: Map<PlayerId, MulliganState>
    ) : GameStart()
}
