package engine.reducer

import engine.action.DeclareAttackers
import engine.action.PassPriority
import engine.domain.drawCards
import engine.domain.nextInTurnOrder
import engine.domain.startTurn
import engine.state.BeginningOfCombatStep
import engine.state.BeginningPhase
import engine.state.CombatPhase
import engine.state.DeclareAttackersStep
import engine.state.DrawStep
import engine.state.EndOfCombatStep
import engine.state.EndStep
import engine.state.EndingPhase
import engine.state.GameState
import engine.state.PlayerId
import engine.state.PostCombatMainPhase
import engine.state.PreCombatMainPhase
import engine.state.Turn
import engine.state.TurnPhase
import engine.state.UpkeepStep
import engine.state.createManaPool
import engine.state.noPendingRandomization

val turnStepsReducer: GameStatePendingRandomizationReducer = { state, action ->
    when (action) {
        is PassPriority -> passPriority(state.gameState, action).noPendingRandomization()
        is DeclareAttackers -> declareAttackers(state.gameState, action).noPendingRandomization()
        else -> state // not handled by this reducer
    }
}

val turnPhases = listOf(
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
    val turn = mustHavePriority(state, action)
    val nextInPriority = nextInTurnOrder(action.actingPlayer, state.players)
    return if (nextInPriority == turn.activePlayer) {
        goToNextStep(state, turn)
    } else {
        passPriorityWithinStep(state, turn, nextInPriority)
    }
}

private fun declareAttackers(state: GameState, action: DeclareAttackers): GameState {
    val turn = mustBeActivePlayer(state, action)
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
        startNextPhaseOrStep(state, turn, nextPhase)
    }.emptyManaPools()
}

private fun startNextPhaseOrStep(
    state: GameState,
    turn: Turn,
    nextPhase: TurnPhase
): GameState = state.copy(
    players = if (nextPhase == BeginningPhase(step = DrawStep) && !turn.firstTurn) {
        state.replacePlayerState(turn.activePlayer) {
            drawCards(1)
        }
    } else {
        state.players
    },
    temporalPosition = turn.copy(
        phase = nextPhase,
        priority = if (nextPhase == CombatPhase(step = DeclareAttackersStep)) {
            null // active player must declare attacks before receiving priority
        } else {
            turn.activePlayer
        }
    )
)

private fun GameState.emptyManaPools() =
    copy(
        players = players.map {
            it.copy(
                manaPool = createManaPool()
            )
        }
    )

private fun nextPhase(current: TurnPhase): TurnPhase? {
    val currentIndex = turnPhases.indexOf(current)
    require(currentIndex != -1) { "Unrecognized phase or step: $current" }
    return turnPhases.getOrNull(currentIndex + 1)
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
