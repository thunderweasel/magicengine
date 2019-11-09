package engine.domain

import engine.model.Card
import engine.model.GameStart.StartingPlayerMustBeChosen
import engine.model.GameState
import engine.model.MulliganDecision
import engine.model.PlayerId
import engine.model.PlayerState

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
    gameStart = StartingPlayerMustBeChosen(playerDecidesWhoGoesFirst)
)