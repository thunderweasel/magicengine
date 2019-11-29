package engine.action

import engine.model.PlayerId

sealed class PlayerAction : GameAction {
    abstract val actingPlayer: PlayerId
}

data class ChooseFirstPlayer(override val actingPlayer: PlayerId, val chosenPlayer: PlayerId) : PlayerAction()
data class ChooseToKeepHand(
    override val actingPlayer: PlayerId,
    val toBottom: List<Int>
) : PlayerAction()
data class ChooseToMulligan(override val actingPlayer: PlayerId) : PlayerAction()

data class PassPriority(override val actingPlayer: PlayerId) : PlayerAction()
