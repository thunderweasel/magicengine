package engine

import engine.action.PlayerAction
import engine.domain.startingState
import engine.model.Card
import engine.model.GameState
import engine.model.noPendingRandomization
import engine.random.RandomizationResolver
import engine.reducer.GameStatePendingRandomizationReducer
import engine.reducer.masterReducer

class MagicEngine(
    private val reducer: GameStatePendingRandomizationReducer = masterReducer(),
    private val randomizationResolver: RandomizationResolver<GameState> = RandomizationResolver(
        reducer = masterReducer()
    )
) {
    fun start2PlayerGame(deck1: List<Card>, deck2: List<Card>): GameState {
        return randomizationResolver.resolve(startingState(playerDecks = listOf(deck1, deck2)))
    }

    fun performAction(action: PlayerAction, state: GameState): GameState {
        val gameStateAfterPlayerAction = reducer(action, state.noPendingRandomization())
        return randomizationResolver.resolve(gameStateAfterPlayerAction)
    }
}
