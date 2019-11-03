package engine

import engine.action.GameAction
import engine.action.PlayerAction
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

    fun performAction(action: PlayerAction, state: GameState): GameState {
        var statePendingRandomization = reduceGameState(GameAction.ByPlayer(action), state.pendingNoRandomization())
        var pendingAction = statePendingRandomization.pendingAction
        while (pendingAction != null) {
            val randomizationResult = GameAction.RandomizationResult(
                result = RandomResult(
                    action = pendingAction.action,
                    results = pendingAction.pendingRandomization.map {
                        when (it) {
                            is RandomRequest.Shuffle -> shuffler.shuffle(it.cards)
                            is RandomRequest.NumberInRange -> randomizer.randomInt(it.range.first, it.range.last)
                        }
                    }
                )
            )
            statePendingRandomization = reduceGameState(randomizationResult, statePendingRandomization)
            pendingAction = statePendingRandomization.pendingAction
        }
        return statePendingRandomization.gameState
    }
}
