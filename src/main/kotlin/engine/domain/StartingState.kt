package engine.domain

import engine.action.PendingRandomization
import engine.action.RandomizeChoiceForFirst
import engine.model.Card
import engine.model.GameState
import engine.model.PlayerState
import engine.model.RandomRequest
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
    actionOnResolution = RandomizeChoiceForFirst,
    request = RandomRequest(
        shuffles = playerDecks,
        randomNumbers = listOf(1 until playerDecks.size)
    )
))
