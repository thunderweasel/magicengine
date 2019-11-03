package engine.reducer

import engine.action.GameAction
import engine.model.GameStatePendingRandomization

typealias GameStateReducer = (GameAction, GameStatePendingRandomization) -> GameStatePendingRandomization

val reducers = listOf(
    gameStartReducer
)

fun reduceGameState(action: GameAction, state: GameStatePendingRandomization): GameStatePendingRandomization =
    reducers.fold(state) { state, reducer ->
        reducer(action, state)
    }
