package engine.domain

import engine.action.ElectDeciderOfStartingPlayer
import engine.action.PendingRandomization
import engine.model.Card
import engine.model.GameState
import engine.model.PlayerState
import engine.model.RandomRequest
import engine.model.Range
import engine.model.StartingPlayerMustBeChosen
import engine.model.pendingRandomization

fun startingState(
    playerDecks: List<List<Card>>
) = GameState(
    players = playerDecks.mapIndexed { index, deck ->
        PlayerState(
            id = index + 1,
            library = deck,
            lifeTotal = 20
        )
    },
    gameStart = StartingPlayerMustBeChosen(player = null)
).pendingRandomization(PendingRandomization(
    actionOnResolution = ElectDeciderOfStartingPlayer,
    request = RandomRequest(
        shuffles = playerDecks,
        randomNumbers = listOf(Range(1 until playerDecks.size))
    )
))
