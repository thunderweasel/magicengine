package engine.assertions

import assertk.Assert
import assertk.all
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import engine.action.GameAction
import engine.reducer.StatePendingRandomizationReducer
import engine.reducer.masterReducer
import engine.state.GameState
import engine.state.StatePendingRandomization
import engine.state.noPendingRandomization

fun Assert<GameState>.actionResultsInState(
    action: GameAction,
    expectedState: GameState
) = transform { actual ->
    actual.noPendingRandomization()
}.actionResultsInState(
    reducer = masterReducer(),
    action = action,
    expectedState = expectedState
)

fun <STATE_TYPE> Assert<StatePendingRandomization<STATE_TYPE>>.actionResultsInState(
    reducer: StatePendingRandomizationReducer<STATE_TYPE>,
    action: GameAction,
    expectedState: STATE_TYPE
) = transform { actual ->
    reducer(actual, action)
}.matchesState(expectedState)

fun <STATE_TYPE> Assert<StatePendingRandomization<STATE_TYPE>>.matchesState(expected: STATE_TYPE) =
    all {
        prop(StatePendingRandomization<STATE_TYPE>::pendingAction).isNull()
        prop(StatePendingRandomization<STATE_TYPE>::gameState).isEqualTo(expected)
    }
