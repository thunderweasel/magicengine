package engine.reducer

import engine.action.GameAction
import engine.domain.drawCards
import engine.model.GameStart
import engine.model.GameState

fun reduceGameState(action: GameAction, state: GameState) =
    when(action) {
        is GameAction.ChooseFirstPlayer -> state.copy(
            players = state.players.map { it.drawCards(7) },
            gameStart = GameStart.Mulligans(action.chosenPlayer)
        )
    }