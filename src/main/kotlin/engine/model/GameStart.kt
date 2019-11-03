package engine.model

sealed class GameStart {
    data class FirstPlayerMustBeChosenBy(val player: PlayerId): GameStart()
    data class ResolvingMulligans(val currentChoice: PlayerId): GameStart()
    object GameStarted: GameStart()
}
