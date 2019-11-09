package engine.reducer

import engine.action.GameAction
import engine.model.GameStatePendingRandomization

typealias GameStateReducer = (GameAction, GameStatePendingRandomization) -> GameStatePendingRandomization

val reducers = listOf(
    gameStartStateReducer
)

fun reduceGameState(action: GameAction, state: GameStatePendingRandomization): GameStatePendingRandomization =
    reducers.fold(state) { foldedState, reducer ->
        reducer(action, foldedState)
    }
