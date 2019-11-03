package engine.random

import engine.action.RandomizationResult
import engine.model.Card
import engine.model.GameState
import engine.model.GameStatePendingRandomization
import engine.model.RandomRequest
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

            val randomizationResult = RandomizationResult(
                values = pendingAction.pendingRandomization.map { request ->
                    when (request) {
                        is RandomRequest.Shuffle -> shuffler.shuffle(request.cards)
                        is RandomRequest.NumberInRange -> randomizer.randomInt(request.range.first, request.range.last)
                    }
                }
            )
            resolvingState = reducer(randomizationResult, resolvingState)
        }

        return resolvingState.gameState
    }
}