package engine.action

import engine.model.PlayerId

sealed class PlayerAction : GameAction {
    data class ChooseFirstPlayer(val chosenPlayer: PlayerId) : PlayerAction()
    object KeepHand : PlayerAction()
    object Mulligan : PlayerAction()
}