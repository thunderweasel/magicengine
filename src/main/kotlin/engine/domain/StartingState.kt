package engine.domain

import engine.action.ElectDeciderOfStartingPlayer
import engine.action.PendingRandomization
import engine.state.Card
import engine.state.GameState
import engine.state.PlayerState
import engine.state.RandomRequest
import engine.state.Range
import engine.state.StartingPlayerMustBeChosen
import engine.state.pendingRandomization

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
    temporalPosition = StartingPlayerMustBeChosen(player = null)
).pendingRandomization(PendingRandomization(
    actionOnResolution = ElectDeciderOfStartingPlayer,
    request = RandomRequest(
        shuffles = playerDecks,
        randomNumbers = listOf(Range(1 until playerDecks.size))
    )
))
