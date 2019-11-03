package engine.model

sealed class GameStart {
    data class PlayerMustDecideWhoGoesFirst(val playerId: Int) : GameStart()
    data class Mulligans(
        val currentPlayer: Int,
        val resolvedMulligans: List<Int>
    ) : GameStart()
}
