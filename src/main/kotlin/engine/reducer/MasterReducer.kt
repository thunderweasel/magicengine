package engine.reducer

import engine.action.GameAction
import engine.formats.MagicFormat
import engine.state.GameState
import engine.state.StatePendingRandomization

/**
 * All actions taken on the state are ultimately passed through this reducer. The contents of this file should be kept
 * simple.
 */
typealias StatePendingRandomizationReducer<T> = (StatePendingRandomization<T>, GameAction) -> StatePendingRandomization<T>
typealias GameStatePendingRandomizationReducer = StatePendingRandomizationReducer<GameState>
fun masterReducer(
    format: MagicFormat,
    children: List<GameStatePendingRandomizationReducer> = childReducers(format = format)
): GameStatePendingRandomizationReducer = { state, action ->
        children.fold(state) { foldedState, reducer ->
            reducer(foldedState, action)
        }
    }
private fun childReducers(format: MagicFormat) = listOf(
    gameStartReducer,
    turnStepsReducer,
    playLandsReducer(format = format),
    activateAbilityReducer(format = format)
)
