package engine.reducer

import engine.action.GameAction
import engine.model.GameStatePendingRandomization

/**
 * All actions taken on the state are ultimately passed through this reducer. The contents of this file should be kept
 * simple.
 */
typealias GameStatePendingRandomizationReducer = (GameAction, GameStatePendingRandomization) -> GameStatePendingRandomization
fun masterReducer(children: List<GameStatePendingRandomizationReducer> = childReducers): GameStatePendingRandomizationReducer =
    { action, state ->
        childReducers.fold(state) { foldedState, reducer ->
            reducer(action, foldedState)
        }
    }
private val childReducers: List<GameStatePendingRandomizationReducer> = listOf(
    gameStartStateReducer
)