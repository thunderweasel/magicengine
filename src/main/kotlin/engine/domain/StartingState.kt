package engine.domain

import engine.action.ElectDeciderOfStartingPlayer
import engine.action.PendingRandomization
import engine.state.Card
import engine.state.GameState
import engine.state.PlayerState
import engine.state.RandomRequest
import engine.state.Range
import engine.state.StartingPlayerMustBeChosen
import engine.state.StatePendingRandomization
import engine.state.pendingRandomization

fun startingState(
    playerDecks: List<List<String>>
): StatePendingRandomization<GameState> {
    var prevId = 0
    val decksOfCards = playerDecks
        .map { cardNames: List<String> ->
            cardNames
                .map { cardName ->
                    prevId++
                    Card.KnownCard(
                        id = prevId,
                        name = cardName
                    )
                }
        }
    return GameState(
        players = decksOfCards.mapIndexed { index, deck ->
            PlayerState(
                id = index + 1,
                library = deck,
                lifeTotal = 20
            )
        },
        temporalPosition = StartingPlayerMustBeChosen(player = null),
        nextCardId = prevId + 1
    ).pendingRandomization(PendingRandomization(
        actionOnResolution = ElectDeciderOfStartingPlayer,
        request = RandomRequest(
            shuffles = decksOfCards.map { it.size },
            randomNumbers = listOf(Range(1 until playerDecks.size))
        )
    ))
}
