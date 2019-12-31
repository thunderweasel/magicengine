package engine.action

import engine.state.AbilityId
import engine.state.CardId
import engine.state.PermanentId
import engine.state.PlayerId

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
// TODO: should actually list attackers
data class DeclareAttackers(override val actingPlayer: PlayerId) : PlayerAction()

data class PlayLand(override val actingPlayer: PlayerId, val cardId: CardId) : PlayerAction()
data class ActivateAbility(
    override val actingPlayer: PlayerId,
    val permanentId: PermanentId,
    val abilityId: AbilityId
) : PlayerAction()
