package engine.action

import engine.domain.PlayerId

sealed class GameAction {
    data class ChooseFirstPlayer(val chosenPlayer: PlayerId): GameAction()
}