package engine.domain

import engine.model.Card
import engine.model.GameStart
import engine.model.GameState
import engine.model.PlayerState

fun startingState(
    shuffledPlayerDecks: List<List<Card>>,
    playerDecidesWhoGoesFirst: Int
) = GameState(
    players = shuffledPlayerDecks.mapIndexed { index, deck ->
        PlayerState(
            id = index + 1,
            library = deck,
            lifeTotal = 20
        )
    },
    gameStart = GameStart.PlayerMustDecideWhoGoesFirst(playerId = playerDecidesWhoGoesFirst)
)