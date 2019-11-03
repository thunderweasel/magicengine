package engine

import engine.action.PlayerAction
import engine.domain.startingState
import engine.model.*
import engine.reducer.reduceGameState
import engine.random.ActualRandomizer
import engine.random.RandomShuffler
import engine.random.Randomizer
import engine.random.Shuffler
import engine.model.noPendingRandomization
import engine.random.RandomizationResolver
import engine.reducer.GameStateReducer

class MagicEngine(
    val shuffler: Shuffler<Card> = RandomShuffler(),
    val randomizer: Randomizer = ActualRandomizer(),
    val reducer: GameStateReducer = ::reduceGameState
) {
    private val randomizationResolver = RandomizationResolver(
        shuffler = shuffler,
        randomizer = randomizer,
        reducer = reducer
    )

    fun start2PlayerGame(deck1: List<Card>, deck2: List<Card>): GameState =
        startingState(
            shuffledPlayerDecks = listOf(deck1, deck2).map { shuffler.shuffle(it) },
            playerDecidesWhoGoesFirst = randomizer.randomInt(1, 2)
        )

    fun performAction(action: PlayerAction, state: GameState): GameState {
        val gameStateAfterPlayerAction = reducer(action, state.noPendingRandomization())
        return randomizationResolver.resolve(gameStateAfterPlayerAction)
    }
}
