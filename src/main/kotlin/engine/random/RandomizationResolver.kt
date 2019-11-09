package engine.random

import engine.action.RandomizedResultAction
import engine.action.ResolvedRandomization
import engine.model.Card
import engine.model.GameState
import engine.model.GameStatePendingRandomization
import engine.reducer.GameStateReducer

class RandomizationResolver(
    val reducer: GameStateReducer,
    val shuffler: Shuffler<Card>,
    val randomizer: Randomizer
) {
    fun resolve(state: GameStatePendingRandomization): GameState {
        var resolvingState: GameStatePendingRandomization = state
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