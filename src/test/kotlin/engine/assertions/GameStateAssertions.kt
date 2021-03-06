package engine.assertions

import assertk.Assert
import assertk.all
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import engine.action.GameAction
import engine.action.PlayerAction
import engine.formats.AllSpellsAreBurnSpells
import engine.formats.MagicFormat
import engine.reducer.StatePendingRandomizationReducer
import engine.reducer.masterReducer
import engine.state.GameState
import engine.state.StatePendingRandomization
import engine.state.noPendingRandomization

fun Assert<GameState>.actionResultsInState(
    action: GameAction,
    expectedState: GameState,
    format: MagicFormat = AllSpellsAreBurnSpells()
) = transform { actual ->
    actual.noPendingRandomization()
}.actionResultsInState(
    reducer = masterReducer(format = format),
    action = action,
    expectedState = expectedState
)

fun Assert<GameState>.actionResultsInError(
    action: PlayerAction,
    expectedError: Throwable,
    format: MagicFormat = AllSpellsAreBurnSpells()
) = transform { actual ->
    actual.noPendingRandomization()
}.actionResultsInError(
    reducer = masterReducer(format = format),
    action = action,
    expectedError = expectedError
)

fun <STATE_TYPE> Assert<StatePendingRandomization<STATE_TYPE>>.actionResultsInError(
    reducer: StatePendingRandomizationReducer<STATE_TYPE>,
    action: GameAction,
    expectedError: Throwable
) = transform { actual ->
    kotlin.runCatching { reducer(actual, action) }
}.prop("exceptionOrNull") { it.exceptionOrNull() }
    .isNotNull()
    .isEqualTo(expectedError)

fun <STATE_TYPE> Assert<StatePendingRandomization<STATE_TYPE>>.actionResultsInState(
    reducer: StatePendingRandomizationReducer<STATE_TYPE>,
    action: GameAction,
    expectedState: STATE_TYPE
) = transform { actual ->
    reducer(actual, action)
}.isResolvedAs(expectedState)

fun <STATE_TYPE> Assert<StatePendingRandomization<STATE_TYPE>>.isResolvedAs(expected: STATE_TYPE) =
    all {
        prop(StatePendingRandomization<STATE_TYPE>::pendingAction).isNull()
        prop(StatePendingRandomization<STATE_TYPE>::gameState).isEqualTo(expected)
    }
