package engine.reducer

import engine.action.DeclareAttackers
import engine.action.PassPriority
import engine.domain.nextInTurnOrder
import engine.domain.startTurn
import engine.model.BeginningOfCombatStep
import engine.model.BeginningPhase
import engine.model.CombatPhase
import engine.model.DeclareAttackersStep
import engine.model.DrawStep
import engine.model.EndOfCombatStep
import engine.model.EndStep
import engine.model.EndingPhase
import engine.model.GameState
import engine.model.InvalidPlayerAction
import engine.model.PlayerId
import engine.model.PostCombatMainPhase
import engine.model.PreCombatMainPhase
import engine.model.Turn
import engine.model.TurnPhase
import engine.model.UpkeepStep
import engine.model.noPendingRandomization

val turnStepsReducer: GameStatePendingRandomizationReducer = { state, action ->
    when (action) {
        is PassPriority -> passPriority(state.gameState, action).noPendingRandomization()
        is DeclareAttackers -> declareAttackers(state.gameState, action).noPendingRandomization()
        else -> state // not handled by this reducer
    }
}

private val turnOrder = listOf(
    // TODO: Will add this in when there are permanents to untap (no priority pass on untap step)
    // BeginningPhase(step = UntapStep),
    BeginningPhase(step = UpkeepStep),
    BeginningPhase(step = DrawStep),
    PreCombatMainPhase,
    CombatPhase(step = BeginningOfCombatStep),
    CombatPhase(step = DeclareAttackersStep),
    // TODO: Once creatures exist and attacks can happen, these steps will not be skipped
    // CombatPhase(step = DeclareBlockersStep),
    // CombatPhase(step = CombatDamageStep),
    CombatPhase(step = EndOfCombatStep),
    PostCombatMainPhase,
    EndingPhase(step = EndStep)
    // TODO: Cleanup step will be relevant later
    // EndingPhase(step = CleanupStep)
)

private fun passPriority(state: GameState, action: PassPriority): GameState {
    val turn = state.temporalPosition
    require(turn is Turn)
    if (action.actingPlayer != turn.priority) {
        throw InvalidPlayerAction(
            action = action,
            state = state,
            reason = "Player ${action.actingPlayer} does not have priority"
        )
    }
    val nextInPriority = nextInTurnOrder(turn.priority, state.players)
    return if (nextInPriority == turn.activePlayer) {
        goToNextStep(state, turn)
    } else {
        passPriorityWithinStep(state, turn, nextInPriority)
    }
}

private fun declareAttackers(state: GameState, action: DeclareAttackers): GameState {
    val turn = state.temporalPosition
    require(turn is Turn)
    if (action.actingPlayer != turn.activePlayer) {
        throw InvalidPlayerAction(
            action = action,
            state = state,
            reason = "Player ${action.actingPlayer} is not the active player"
        )
    }
    return state.copy(
        temporalPosition = turn.copy(
            priority = turn.activePlayer
        )
    )
}

private fun goToNextStep(state: GameState, turn: Turn): GameState {
    val nextPhase = nextPhase(turn.phase)
    return if (nextPhase == null) {
        endTurn(state, turn)
    } else {
        state.copy(
            temporalPosition = turn.copy(
                phase = nextPhase,
                priority = if (nextPhase == CombatPhase(step = DeclareAttackersStep)) {
                    null // active player must declare attacks before receiving priority
                } else {
                    turn.activePlayer
                }
            )
        )
    }
}

private fun nextPhase(current: TurnPhase): TurnPhase? {
    val currentIndex = turnOrder.indexOf(current)
    require(currentIndex != -1) { "Unrecognized phase or step: $current" }
    return turnOrder.getOrNull(currentIndex + 1)
}

private fun endTurn(state: GameState, turn: Turn) =
    state.startTurn(nextInTurnOrder(turn.activePlayer, state.players))

private fun passPriorityWithinStep(
    state: GameState,
    turn: Turn,
    nextInPriority: PlayerId
) = state.copy(
    temporalPosition = turn.copy(
        priority = nextInPriority,
        phase = turn.phase
    )
)
