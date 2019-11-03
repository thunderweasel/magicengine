package engine.reducer

import engine.action.GameAction
import engine.domain.drawCards
import engine.model.GameStart
import engine.model.GameState

private const val STARTING_HAND_SIZE = 7

fun reduceGameState(action: GameAction, state: GameState) =
    when(action) {
        is GameAction.ChooseFirstPlayer -> state.copy(
            players = state.players.map { it.drawCards(STARTING_HAND_SIZE) },
            gameStart = GameStart.Mulligans(
                currentPlayer = action.chosenPlayer,
                resolvedMulligans = listOf()
            )
        )
    }