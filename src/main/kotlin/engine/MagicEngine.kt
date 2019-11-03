package engine

import engine.action.GameAction
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
        GameState(
            players = listOf(deck1, deck2).mapIndexed { index, deck ->
                PlayerState(
                    id = index + 1,
                    library = shuffler.shuffle(deck),
                    lifeTotal = 20
                )
            },
            gameStart = GameStart.PlayerMustDecideWhoGoesFirst(playerId = randomizer.randomInt(1, 2))
        )

    fun performAction(action: GameAction, state: GameState): GameState = reduceGameState(action, state)
}
