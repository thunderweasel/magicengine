package engine

import engine.action.PlayerAction
import engine.domain.startingState
import engine.formats.MagicFormat
import engine.random.ActualRandomizer
import engine.random.RandomShuffler
import engine.random.RandomizationResolver
import engine.reducer.GameStatePendingRandomizationReducer
import engine.reducer.masterReducer
import engine.state.Card
import engine.state.GameState
import engine.state.noPendingRandomization
import kotlin.random.Random

class MagicEngine(
    private val format: MagicFormat,
    private val reducer: GameStatePendingRandomizationReducer = masterReducer(format = format),
    private val randomizationResolver: RandomizationResolver<GameState> = RandomizationResolver(
        reducer = reducer,
        shuffler = RandomShuffler(Random.Default),
        randomizer = ActualRandomizer(Random.Default)
    )
) {
    fun start2PlayerGame(deck1: List<Card>, deck2: List<Card>): GameState {
        return randomizationResolver.resolve(startingState(playerDecks = listOf(deck1, deck2)))
    }

    fun performAction(action: PlayerAction, state: GameState): GameState {
        val gameStateAfterPlayerAction = reducer(state.noPendingRandomization(), action)
        return randomizationResolver.resolve(gameStateAfterPlayerAction)
    }
}
