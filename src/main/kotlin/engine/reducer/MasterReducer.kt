package engine.reducer

import engine.action.GameAction
import engine.model.GameState
import engine.model.StatePendingRandomization

/**
 * All actions taken on the state are ultimately passed through this reducer. The contents of this file should be kept
 * simple.
 */
typealias StatePendingRandomizationReducer<T> = (StatePendingRandomization<T>, GameAction) -> StatePendingRandomization<T>
typealias GameStatePendingRandomizationReducer = StatePendingRandomizationReducer<GameState>
fun masterReducer(children: List<GameStatePendingRandomizationReducer> = childReducers): GameStatePendingRandomizationReducer = { state, action ->
        children.fold(state) { foldedState, reducer ->
            reducer(foldedState, action)
        }
    }
private val childReducers: List<GameStatePendingRandomizationReducer> = listOf(
    gameStartReducer
)
