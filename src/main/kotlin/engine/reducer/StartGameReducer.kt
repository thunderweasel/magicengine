package engine.reducer

import engine.action.GameAction
import engine.action.PendingRandomization
import engine.action.PlayerAction
import engine.action.RandomizedResultAction
import engine.action.RandomizedResultAction.InnerAction.PerformMulligans
import engine.domain.drawCards
import engine.model.GameStart
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
        is GameStart.FirstPlayerMustBeChosenBy -> firstPlayerMustBeChosenReducer(action, state)
        is GameStart.ResolvingMulligans -> mulliganReducer(action, state, state.gameState.gameStart)
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
                gameStart = GameStart.ResolvingMulligans(action.chosenPlayer)
            )
        )
        else -> state
    }

private fun mulliganReducer(
    action: GameAction,
    state: GameStatePendingRandomization,
    mulliganState: GameStart.ResolvingMulligans
): GameStatePendingRandomization {
    val gameState = state.gameState
    return when {
        action is PlayerAction.KeepHand ->
            gameState.copy(
                players = gameState.replacePlayerState(id = mulliganState.currentChoice) {
                    copy(mulliganDecision = MulliganDecision.WILL_KEEP)
                },
                gameStart = GameStart.ResolvingMulligans(
                    currentChoice = 1 // TODO: obviously wrong
                )
            ).noPendingRandomization()
        action is PlayerAction.Mulligan -> {
            gameState.copy(
                players = gameState.replacePlayerState(mulliganState.currentChoice) {
                    copy(
                        mulliganDecision = MulliganDecision.WILL_MULLIGAN,
                        hand = emptyList(),
                        library = hand.plus(library)
                    )
                }
            )
                // TODO: Should check at end of PlayerAction.KeepHand as well
                .pendingRandomization {
                    PendingRandomization(
                        actionOnResolution = PerformMulligans,
                        request = RandomRequest(
                            shuffles = players
                                .filter { it.mulliganDecision == MulliganDecision.WILL_MULLIGAN }
                                .map { it.library }
                        )
                    )
                }
        }
        action is RandomizedResultAction && action.innerAction == PerformMulligans ->
            state.gameState.copy(
                players = state.gameState.players
                    .map { playerState ->
                        if (playerState.mulliganDecision == MulliganDecision.WILL_MULLIGAN) {
                            playerState.copy(
                                // TODO: obviously wrong for multiple mulligans
                                library = action.resolvedRandomization.completedShuffles[0],
                                mulliganDecision = MulliganDecision.UNDECIDED
                            ).drawCards(STARTING_HAND_SIZE)
                        } else {
                            playerState
                        }
                    }
            ).noPendingRandomization()
        else -> state
    }
}

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
