package engine.random

import engine.action.RandomizedResultAction
import engine.action.ResolvedRandomization
import engine.reducer.StatePendingRandomizationReducer
import engine.state.StatePendingRandomization

class RandomizationResolver<T>(
    private val reducer: StatePendingRandomizationReducer<T>,
    private val shuffler: Shuffler<Int>,
    private val randomizer: Randomizer
) {
    fun resolve(state: StatePendingRandomization<T>): T {
        var resolvingState: StatePendingRandomization<T> = state
        while (true) {
            val pendingAction = resolvingState.pendingAction ?: break

            val pendingRandomization = pendingAction.request
            val resolvedRandomization = ResolvedRandomization(
                completedShuffles = pendingRandomization.shuffles.map {
                    shuffler.shuffle((0 until it).toList())
                },
                generatedNumbers = pendingRandomization.randomNumbers.map { randomizer.randomInt(it.first, it.last) }
            )
            resolvingState =
                reducer(resolvingState, RandomizedResultAction(pendingAction.actionOnResolution, resolvedRandomization))
        }

        return resolvingState.gameState
    }
}
