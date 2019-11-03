package engine.action

import engine.domain.PlayerId

sealed class PlayerAction {
    data class ChooseFirstPlayer(val chosenPlayer: PlayerId): PlayerAction()
    object KeepHand: PlayerAction()
    object Mulligan: PlayerAction()
}