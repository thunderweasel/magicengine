package engine.reducer

import engine.action.GameAction
import engine.action.PendingRandomizedAction
import engine.action.PlayerAction
import engine.domain.MulliganState
import engine.domain.drawCards
import engine.model.*

private const val STARTING_HAND_SIZE = 7

fun reduceGameState(action: GameAction, state: GameStatePendingRandomization) =
    when (action) {
        is GameAction.ByPlayer -> handlePlayerAction(action.playerAction, state.gameState)
        is GameAction.RandomizationResult -> handleRandomizationResult(action, state)
    }

private fun handlePlayerAction(action: PlayerAction, state: GameState): GameStatePendingRandomization =
    when (action) {
        is PlayerAction.ChooseFirstPlayer ->
            state.copy(
                players = state.players.map { it.drawCards(STARTING_HAND_SIZE) },
                gameStart = GameStart.Mulligans(
                    currentPlayer = action.chosenPlayer,
                    mulliganStates = state.players
                        .map { it.id to MulliganState.UNDECIDED }
                        .toMap()
                )
            ).pendingNoRandomization()
        is PlayerAction.KeepHand ->
            state.copy(
                gameStart = GameStart.Mulligans(
                    currentPlayer = ((state.gameStart as GameStart.Mulligans).currentPlayer + 1) % 2,
                    mulliganStates = state.gameStart.mulliganStates.plus(state.gameStart.currentPlayer to MulliganState.WILL_KEEP)
                )
            ).pendingNoRandomization()
        is PlayerAction.Mulligan ->
            state.copy(
                gameStart = GameStart.Mulligans(
                    currentPlayer = (state.gameStart as GameStart.Mulligans).currentPlayer,
                    mulliganStates = state.gameStart.mulliganStates.plus(state.gameStart.currentPlayer to MulliganState.WILL_MULLIGAN)
                )
            ).pendingRandomization {
                PendingRandomizedAction(
                    action,
                    (gameStart as GameStart.Mulligans).mulliganStates
                        .filter { it.value == MulliganState.WILL_MULLIGAN }
                            // TODO: this is stupid
                        .map { RandomRequest.Shuffle(players[it.key - 1].hand.plus(players[it.key - 1].library)) }
                )
            }
    }

private fun handleRandomizationResult(randomizationResult: GameAction.RandomizationResult, state: GameStatePendingRandomization) =
    when (randomizationResult.result.action) {
        is PlayerAction.Mulligan -> {
            state.gameState.copy(
                players = state.gameState.players
                    .map { playerState ->
                        if ((state.gameState.gameStart as GameStart.Mulligans).mulliganStates[playerState.id] == MulliganState.WILL_MULLIGAN) {
                            playerState.copy(
                                // TODO: obviously wrong for multiple mulligans
                                library = randomizationResult.result.results.first() as List<Card>
                            ).drawCards(STARTING_HAND_SIZE)
                        } else {
                            playerState
                        }
                    },
                gameStart = (state.gameState.gameStart as GameStart.Mulligans).copy(
                    mulliganStates = state.gameState.gameStart.mulliganStates
                        .mapValues { (_, mulliganState) ->
                            if (mulliganState == MulliganState.WILL_MULLIGAN) {
                                MulliganState.UNDECIDED
                            } else {
                                mulliganState
                            }
                        }
                )
            ).pendingNoRandomization()
        }
        else -> state
    }