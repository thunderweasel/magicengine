package engine

import engine.model.Card
import engine.model.CardId
import engine.model.GameState
import engine.model.PlayerId
import engine.model.PlayerState

fun GameState.viewAs(viewer: PlayerId): GameState {
    require(this.viewer == null) { "GameState already has a viewer!"}
    var nextCardId: CardId = 1
    fun generateCardId(): CardId = nextCardId++
    return GameState(
        viewer = viewer,
        players = players.map { player ->
            PlayerState(
                id = player.id,
                lifeTotal = player.lifeTotal,
                hand = if (player.id == viewer) {
                    player.hand.map {
                        require(it is Card.KnownCard)
                        Card.KnownCard(it.name, generateCardId())
                    }
                } else {
                    player.hand.map { Card.UnknownCard(generateCardId()) }
                },
                library = player.library.map { Card.UnknownCard(generateCardId()) }
            )
        },
        gameStart = gameStart
    )
}