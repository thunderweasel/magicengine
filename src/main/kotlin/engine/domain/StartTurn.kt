package engine.domain

import engine.state.BeginningPhase
import engine.state.GameState
import engine.state.PlayerId
import engine.state.Turn
import engine.state.UpkeepStep

fun GameState.startTurn(activePlayer: PlayerId, firstTurn: Boolean = false): GameState =
    // TODO: Untap step should occur (when, you know, permanents exist)
    copy(
        temporalPosition = Turn(
            activePlayer = activePlayer,
            phase = BeginningPhase(step = UpkeepStep),
            priority = activePlayer,
            firstTurn = firstTurn
        )
    )
