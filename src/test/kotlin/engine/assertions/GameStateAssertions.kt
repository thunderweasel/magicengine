package engine.assertions

import assertk.Assert
import assertk.all
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import engine.model.StatePendingRandomization

fun <STATE_TYPE> Assert<StatePendingRandomization<STATE_TYPE>>.matchesState(expected: STATE_TYPE) =
    all {
        prop(StatePendingRandomization<STATE_TYPE>::pendingAction).isNull()
        prop(StatePendingRandomization<STATE_TYPE>::gameState).isEqualTo(expected)
    }
