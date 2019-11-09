package engine.reducer

import engine.action.GameAction
import engine.action.PendingRandomization
import engine.action.PlayerAction
import engine.action.RandomizedResultAction
import engine.action.RandomizedResultAction.InnerAction.PerformMulligans
import engine.domain.drawCards
import engine.domain.firstInTurnOrder
import engine.domain.nextInTurnOrder
import engine.model.GameStart.ResolvingMulligans
import engine.model.GameStart.StartingPlayerMustBeChosen
import engine.model.GameState
import engine.model.GameStatePendingRandomization
import engine.model.MulliganDecision
import engine.model.PlayerId
import engine.model.PlayerState
import engine.model.RandomRequest
import engine.model.noPendingRandomization
import engine.model.pendingRandomization

private const val STARTING_HAND_SIZE = 7

val gameStartStateReducer: GameStateReducer = { action, state ->
    when (state.gameState.gameStart) {
        is StartingPlayerMustBeChosen -> firstPlayerMustBeChosenReducer(action, state)
        is ResolvingMulligans -> mulliganReducer(action, state, state.gameState.gameStart)
        else -> state
    }
}

private fun firstPlayerMustBeChosenReducer(
    action: GameAction,
    state: GameStatePendingRandomization
): GameStatePendingRandomization =
    when (action) {
        is PlayerAction.ChooseFirstPlayer -> state.copy(
            gameState = state.gameState.copy(
                players = state.gameState.players.map { it.drawCards(STARTING_HAND_SIZE) },
                gameStart = ResolvingMulligans(
                    startingPlayer = action.chosenPlayer,
                    currentChoice = action.chosenPlayer
                )
            )
        )
        else -> state
    }

private fun mulliganReducer(
    action: GameAction,
    state: GameStatePendingRandomization,
    mulliganState: ResolvingMulligans
): GameStatePendingRandomization {
    val gameState = state.gameState
    return when {
        action is PlayerAction.KeepHand ->
            gameState.copy(
                players = gameState.replacePlayerState(id = mulliganState.currentChoice) {
                    copy(mulliganDecision = MulliganDecision.WILL_KEEP)
                },
                gameStart = mulliganState.copy(
                    currentChoice = nextPlayerAfterMulliganDecision(mulliganState, gameState)
                )
            ).checkIfAllPlayersDecidedMulligans()
        action is PlayerAction.Mulligan -> {
            gameState.copy(
                players = gameState.replacePlayerState(mulliganState.currentChoice) {
                    copy(
                        mulliganDecision = MulliganDecision.WILL_MULLIGAN,
                        hand = emptyList(),
                        library = hand.plus(library)
                    )
                },
                gameStart = mulliganState.copy(
                    currentChoice = nextPlayerAfterMulliganDecision(mulliganState, gameState)
                )
            ).checkIfAllPlayersDecidedMulligans()
        }
        action is RandomizedResultAction && action.innerAction == PerformMulligans -> {
            state.gameState.copy(
                players = state.gameState.players.replacePlayerStates(
                    action.resolvedRandomization.completedShuffles
                        .zip(state.gameState.players.filter(whoMulled))
                        .map { (completedShuffle, playerState) ->
                            playerState.copy(
                                library = completedShuffle,
                                mulliganDecision = MulliganDecision.UNDECIDED
                            ).drawCards(STARTING_HAND_SIZE)
                        }
                )
            )
                .run {
                    copy(
                        gameStart = mulliganState.copy(
                            currentChoice = firstInTurnOrder(mulliganState.startingPlayer, players) { it.mulliganDecision == MulliganDecision.UNDECIDED }
                                ?: TODO("Start the damn game!")
                        )
                    )
                }
                .noPendingRandomization()
        }
        else -> state
    }
}

private fun nextPlayerAfterMulliganDecision(
    mulliganState: ResolvingMulligans,
    gameState: GameState
) =
    nextInTurnOrder(mulliganState.currentChoice, gameState.players) { it.mulliganDecision == MulliganDecision.UNDECIDED }
        ?: mulliganState.startingPlayer

private fun GameState.checkIfAllPlayersDecidedMulligans(): GameStatePendingRandomization =
    pendingRandomization {
        if (players.any(undecided)) {
            null
        } else {
            PendingRandomization(
                actionOnResolution = PerformMulligans,
                request = RandomRequest(
                    shuffles = players.filter(whoMulled).map { it.library }
                )
            )
        }
    }

private val whoMulled = { it: PlayerState -> it.mulliganDecision == MulliganDecision.WILL_MULLIGAN }
private val undecided = { it: PlayerState -> it.mulliganDecision == MulliganDecision.UNDECIDED }

private inline fun GameState.replacePlayerState(
    id: PlayerId,
    crossinline compute: PlayerState.() -> PlayerState
) = players.map {
    if (it.id == id) {
        it.compute()
    } else {
        it
    }
}

private fun List<PlayerState>.replacePlayerStates(
    playersToReplace: List<PlayerState>
) = map { existingPlayer ->
    playersToReplace.firstOrNull { it.id == existingPlayer.id } ?: existingPlayer
}