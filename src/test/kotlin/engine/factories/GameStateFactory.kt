package engine.factories

import engine.state.Battlefield
import engine.state.CardId
import engine.state.GameState
import engine.state.PlayerId
import engine.state.PlayerState
import engine.state.TemporalPosition
import engine.state.createBattlefield

object GameStateFactory {
    fun create(
        viewer: PlayerId? = null,
        players: List<PlayerState> = PlayerStateFactory.createAliceAndBobWithStartingHands(),
        battlefield: Battlefield = createBattlefield(),
        temporalPosition: TemporalPosition,
        nextCardId: CardId = 121 // Assuming two 60 card decks and no re-generation, this will be the next ID
    ): GameState = GameState(
        viewer = viewer,
        players = players,
        battlefield = battlefield,
        temporalPosition = temporalPosition,
        nextCardId = nextCardId
    )
}
