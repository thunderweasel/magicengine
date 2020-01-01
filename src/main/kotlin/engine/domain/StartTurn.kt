package engine.domain

import engine.state.BeginningPhase
import engine.state.GameState
import engine.state.PlayerId
import engine.state.Turn
import engine.state.UpkeepStep

fun GameState.startTurn(activePlayer: PlayerId, firstTurn: Boolean = false): GameState =
    copy(
        battlefield = battlefield.mapValues { (_, permanent) ->
            if (permanent.controller == activePlayer) {
                permanent.copy(
                    tapped = false
                )
            } else {
                permanent
            }
        },
        temporalPosition = Turn(
            activePlayer = activePlayer,
            phase = BeginningPhase(step = UpkeepStep),
            priority = activePlayer,
            firstTurn = firstTurn
        )
    )
