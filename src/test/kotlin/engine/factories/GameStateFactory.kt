package engine.factories

import engine.state.Battlefield
import engine.state.GameState
import engine.state.IdState
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
        nextCardId: Int = 121,
        nextPermanentId: Long = 1L
    ): GameState = GameState(
        viewer = viewer,
        players = players,
        battlefield = battlefield,
        temporalPosition = temporalPosition,
        idState = IdState(
            nextCardId = nextCardId,
            nextPermanentId = nextPermanentId
        )
    )
}
