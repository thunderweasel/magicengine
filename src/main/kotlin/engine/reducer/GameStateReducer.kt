package engine.reducer

import engine.action.GameAction
import engine.action.PendingRandomizedAction
import engine.action.PlayerAction
import engine.model.MulliganDecision
import engine.domain.drawCards
import engine.model.Card
import engine.model.GameState
import engine.model.GameStatePendingRandomization
import engine.model.noRandomization
import engine.model.GamePosition.StartingGameState.ResolvingMulligans
import engine.model.RandomRequest
import engine.model.pendingRandomization

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
                gamePosition = ResolvingMulligans(action.chosenPlayer)
            ).noRandomization()
        is PlayerAction.KeepHand -> {
            val mulliganState = state.gamePosition as ResolvingMulligans
            state.copy(
                players = state.players.map {
                    if (it.id == mulliganState.currentChoice) {
                        it.copy(
                            mulliganDecision = MulliganDecision.WILL_KEEP
                        )
                    } else {
                        it
                    }
                },
                gamePosition = ResolvingMulligans(
                    currentChoice = 1 // TODO: obviously wrong
                )
            ).noRandomization()
        }
        is PlayerAction.Mulligan -> {
            val mulliganState = state.gamePosition as ResolvingMulligans
            state.copy(
                players = state.players.map { prevPlayerState ->
                    if (prevPlayerState.id == mulliganState.currentChoice) {
                        prevPlayerState.copy(
                            mulliganDecision = MulliganDecision.WILL_MULLIGAN,
                            hand = listOf(),
                            library = prevPlayerState.hand.plus(prevPlayerState.library)
                        )
                    } else {
                        prevPlayerState
                    }
                }
            )
                .pendingRandomization {
                    PendingRandomizedAction(
                        action = action,
                        pendingRandomization = players
                            .filter { it.mulliganDecision == MulliganDecision.WILL_MULLIGAN }
                            .map { RandomRequest.Shuffle(it.library) }
                    )
                }
        }
    }


private fun handleRandomizationResult(randomizationResult: GameAction.RandomizationResult, state: GameStatePendingRandomization) =
    when (randomizationResult.result.action) {
        is PlayerAction.Mulligan -> {
            state.gameState.copy(
                players = state.gameState.players
                    .map { playerState ->
                        if (playerState.mulliganDecision == MulliganDecision.WILL_MULLIGAN) {
                            playerState.copy(
                                // TODO: obviously wrong for multiple mulligans
                                library = randomizationResult.result.results.first() as List<Card>,
                                mulliganDecision = MulliganDecision.UNDECIDED
                            ).drawCards(STARTING_HAND_SIZE)
                        } else {
                            playerState
                        }
                    }
            ).noRandomization()
        }
        else -> state
    }