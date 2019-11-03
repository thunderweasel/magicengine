package engine

import engine.action.GameAction
import engine.domain.startingState
import engine.model.*
import engine.reducer.reduceGameState
import engine.shuffler.ActualRandomizer
import engine.shuffler.RandomShuffler
import engine.shuffler.Randomizer
import engine.shuffler.Shuffler

class MagicEngine(
    val shuffler: Shuffler<Card> = RandomShuffler(),
    val randomizer: Randomizer = ActualRandomizer()
) {
    fun start2PlayerGame(deck1: List<Card>, deck2: List<Card>): GameState =
        startingState(
            shuffledPlayerDecks = listOf(deck1, deck2).map { shuffler.shuffle(it) },
            playerDecidesWhoGoesFirst = randomizer.randomInt(1, 2)
        )

    fun performAction(action: GameAction, state: GameState): GameState = reduceGameState(action, state)
}
