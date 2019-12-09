package engine.reducer

import engine.action.ActivateAbility
import engine.cards.ActivatedAbilitySpec
import engine.cards.lookupSpec
import engine.domain.Cost
import engine.formats.MagicFormat
import engine.state.ActivatedAbility
import engine.state.GameState
import engine.state.InvalidPlayerAction
import engine.state.replacing

fun activateAbilityReducer(format: MagicFormat): GameStatePendingRandomizationReducer = { state, action ->
    when (action) {
        is ActivateAbility -> {
            mustHavePriority(state.gameState, action)
            val permanent = state.gameState.battlefield[action.permanentId] ?: throw InvalidPlayerAction(
                action = action,
                state = state.gameState,
                reason = "There is no permanent ${action.permanentId}"
            )
            if (permanent.controller != action.actingPlayer) {
                throw InvalidPlayerAction(
                    action = action,
                    state = state.gameState,
                    reason = "Players cannot activate abilities on permanents they don't control"
                )
            }
            val ability = permanent.activatedAbilities.find { it.id == action.abilityId } ?: throw InvalidPlayerAction(
                action = action,
                state = state.gameState,
                reason = "There is no ability ${action.abilityId} on permanent ${action.permanentId}"
            )
            val abilitySpec = ability.lookupSpec(format.cardLookup)
            state.gameState
                .payAbilityCosts(abilitySpec, action)
                .resolveAbility(abilitySpec, ability)
        }
        else -> state
    }
}

private fun GameState.payAbilityCosts(abilitySpec: ActivatedAbilitySpec, action: ActivateAbility) =
    abilitySpec.costs.fold(this) { state, cost ->
        when (cost) {
            is Cost.Tap -> state.copy(
                battlefield = state.battlefield.replacing(action.permanentId) {
                    copy(
                        tapped = true
                    )
                }
            )
        }
    }

private fun GameState.resolveAbility(abilitySpec: ActivatedAbilitySpec, ability: ActivatedAbility) =
    abilitySpec.resolve(this, ability)
