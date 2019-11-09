package engine.model

sealed class GameStart {
    data class StartingPlayerMustBeChosen(val player: PlayerId): GameStart()
    data class ResolvingMulligans(val startingPlayer: PlayerId, val currentChoice: PlayerId): GameStart()
    data class GameStarted(val startingPlayer: PlayerId): GameStart()
}
