package engine.model

import engine.domain.PlayerId

sealed class GameStart {
    data class PlayerMustDecideWhoGoesFirst(val playerId: PlayerId) : GameStart()
    data class Mulligans(
        val currentPlayer: Int,
        val resolvedMulligans: List<Int>
    ) : GameStart()
}
