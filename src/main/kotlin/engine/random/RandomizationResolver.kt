package engine.random

import engine.action.GameAction
import engine.action.RandomizedResultAction
import engine.action.ResolvedRandomization
import engine.model.Card
import engine.model.StatePendingRandomization

class RandomizationResolver<T>(
    private val reducer: (GameAction, StatePendingRandomization<T>) -> StatePendingRandomization<T>,
    private val shuffler: Shuffler<Card>,
    private val randomizer: Randomizer
) {
    fun resolve(state: StatePendingRandomization<T>): T {
        var resolvingState: StatePendingRandomization<T> = state
        while (true) {
            val pendingAction = resolvingState.pendingAction ?: break

            val pendingRandomization = pendingAction.request
            val resolvedRandomization = ResolvedRandomization(
                completedShuffles = pendingRandomization.shuffles.map { shuffler.shuffle(it) },
                generatedNumbers = pendingRandomization.randomNumbers.map { randomizer.randomInt(it.first, it.last) }
            )
            resolvingState =
                reducer(RandomizedResultAction(pendingAction.actionOnResolution, resolvedRandomization), resolvingState)
        }

        return resolvingState.gameState
    }
}
