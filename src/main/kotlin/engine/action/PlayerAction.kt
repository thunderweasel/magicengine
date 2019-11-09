package engine.action

import engine.model.PlayerId

sealed class PlayerAction : GameAction {
    data class ChooseFirstPlayer(val chosenPlayer: PlayerId) : PlayerAction()
    data class KeepHand(val toBottom: List<Int>) : PlayerAction()
    object Mulligan : PlayerAction()
}