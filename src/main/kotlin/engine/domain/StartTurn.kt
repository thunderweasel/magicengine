package engine.domain

import engine.model.BeginningPhase
import engine.model.GameState
import engine.model.PlayerId
import engine.model.Turn
import engine.model.UpkeepStep

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
