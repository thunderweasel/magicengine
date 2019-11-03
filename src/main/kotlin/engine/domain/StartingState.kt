package engine.domain

import engine.model.*
import engine.model.GameStart.FirstPlayerMustBeChosenBy

fun startingState(
    shuffledPlayerDecks: List<List<Card>>,
    playerDecidesWhoGoesFirst: PlayerId
) = GameState(
    players = shuffledPlayerDecks.mapIndexed { index, deck ->
        PlayerState(
            id = index + 1,
            library = deck,
            lifeTotal = 20,
            mulliganDecision = MulliganDecision.UNDECIDED
        )
    },
    gameStart = FirstPlayerMustBeChosenBy(playerDecidesWhoGoesFirst)
)