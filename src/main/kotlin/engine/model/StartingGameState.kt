package engine.model

import engine.domain.PlayerId

sealed class GamePosition {
    sealed class StartingGameState : GamePosition() {
        data class FirstPlayerMustBeChosenBy(val player: PlayerId): StartingGameState()
        data class ResolvingMulligans(val currentChoice: PlayerId): StartingGameState()
    }
    object Turn // TODO: Will hold more info later
}
