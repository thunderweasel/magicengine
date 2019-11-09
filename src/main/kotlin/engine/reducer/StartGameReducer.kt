package engine.reducer

import engine.action.GameAction
import engine.action.PendingRandomization
import engine.action.PlayerAction
import engine.action.RandomizedResultAction
import engine.action.RandomizedResultAction.InnerAction.PerformMulligans
import engine.domain.drawCards
import engine.domain.firstInTurnOrder
import engine.domain.nextInTurnOrder
import engine.model.GameStart
import engine.model.GameStart.ResolvingMulligans
import engine.model.GameStart.StartingPlayerMustBeChosen
import engine.model.GameState
import engine.model.GameStatePendingRandomization
import engine.model.InvalidPlayerAction
import engine.model.MulliganDecision
import engine.model.PlayerId
import engine.model.PlayerState
import engine.model.RandomRequest
import engine.model.noPendingRandomization
import engine.model.pendingRandomization

private const val STARTING_HAND_SIZE = 7

val gameStartStateReducer: GameStatePendingRandomizationReducer = { action, state ->
    when (val gameStart = state.gameState.gameStart) {
        is StartingPlayerMustBeChosen -> firstPlayerMustBeChosenStateReduce(action, state.gameState)
        is ResolvingMulligans -> mulliganStateReduce(action, state, gameStart)
        else -> state
    }
}

private fun firstPlayerMustBeChosenStateReduce(
    action: GameAction,
    state: GameState
): GameStatePendingRandomization =
    when (action) {
        is PlayerAction.ChooseFirstPlayer -> drawOpeningHands(state, action)
        else -> state
    }.noPendingRandomization()

private fun drawOpeningHands(
    state: GameState,
    action: PlayerAction.ChooseFirstPlayer
): GameState =
    state.copy(
        players = state.players.map { it.drawCards(STARTING_HAND_SIZE) },
        gameStart = ResolvingMulligans(
            numberOfMulligans = 0,
            startingPlayer = action.chosenPlayer,
            currentChoice = action.chosenPlayer
        )
    )

private fun mulliganStateReduce(
    action: GameAction,
    state: GameStatePendingRandomization,
    mulliganState: ResolvingMulligans
): GameStatePendingRandomization {
    val gameState = state.gameState
    return when {
        action is PlayerAction.KeepHand -> {
            if (action.toBottom.size != mulliganState.numberOfMulligans) throw InvalidPlayerAction(
                action = action,
                state = gameState,
                reason = "toBottom should have size ${mulliganState.numberOfMulligans} but has size ${action.toBottom.size}"
            )
            gameState.makeDecision(mulliganState, MulliganDecision.KEEP)
                .putCardsOnBottom(playerId = mulliganState.currentChoice, toBottom = action.toBottom)
                .checkIfAllPlayersDecidedMulligans(mulliganState)
        }
        action is PlayerAction.Mulligan -> {
            gameState.makeDecision(mulliganState, MulliganDecision.MULLIGAN)
                .putHandBack(playerId = mulliganState.currentChoice)
                .checkIfAllPlayersDecidedMulligans(mulliganState)
        }
        action is RandomizedResultAction && action.innerAction == PerformMulligans -> {
            gameState.performMulligans(action)
                .eachPlayerWhoMulledDecidesWhetherToKeepAgain(mulliganState)
                .noPendingRandomization()
        }
        else -> state
    }
}

private fun GameState.makeDecision(
    mulliganState: ResolvingMulligans,
    mulliganDecision: MulliganDecision
): GameState {
    return copy(
        players = replacePlayerState(id = mulliganState.currentChoice) {
            copy(
                mulliganDecision = mulliganDecision
            )
        },
        gameStart = mulliganState.copy(
            currentChoice = nextPlayerAfterMulliganDecision(this, mulliganState)
        )
    )
}

private fun GameState.putHandBack(
    playerId: PlayerId
): GameState = copy(
    players = replacePlayerState(playerId) {
        copy(
            hand = emptyList(),
            library = hand.plus(library)
        )
    }
)

private fun GameState.putCardsOnBottom(
    playerId: PlayerId,
    toBottom: List<Int>
): GameState = copy(
    players = replacePlayerState(playerId) {
        copy(
            hand = hand.filterIndexed { index, _ -> !toBottom.contains(index) },
            library = library.plus(hand.slice(toBottom))
        )
    }
)

private fun GameState.performMulligans(
    action: RandomizedResultAction
): GameState = copy(
    players = players.replacePlayerStates(
        action.resolvedRandomization.completedShuffles
            .zip(players.filter(mulled))
            .map { (completedShuffle, playerState) ->
                playerState
                    .copy(
                        library = completedShuffle,
                        mulliganDecision = MulliganDecision.UNDECIDED
                    )
                    .drawCards(STARTING_HAND_SIZE)
            }
    )
)

private fun GameState.eachPlayerWhoMulledDecidesWhetherToKeepAgain(mulliganState: ResolvingMulligans): GameState =
    copy(
        gameStart = mulliganState.copy(
            numberOfMulligans = mulliganState.numberOfMulligans + 1,
            currentChoice = firstInTurnOrder(
                mulliganState.startingPlayer,
                players
            ) { it.mulliganDecision == MulliganDecision.UNDECIDED }
                ?: throw IllegalStateException("Unexpected state: no players undecided after performing mulligans")
        )
    )

private fun nextPlayerAfterMulliganDecision(
    gameState: GameState,
    mulliganState: ResolvingMulligans
) =
    nextInTurnOrder(
        mulliganState.currentChoice,
        gameState.players
    ) { it.mulliganDecision == MulliganDecision.UNDECIDED }
        ?: mulliganState.startingPlayer

private fun GameState.checkIfAllPlayersDecidedMulligans(mulliganState: ResolvingMulligans): GameStatePendingRandomization =
    when {
        players.all(kept) ->
            startTheGame(mulliganState).noPendingRandomization()
        players.any(undecided) ->
            // do nothing
            this.noPendingRandomization()
        else ->
            requestRandomizationForPlayersToShuffleDecks()
    }

private fun GameState.startTheGame(mulliganState: ResolvingMulligans): GameState = copy(
    gameStart = GameStart.GameStarted(startingPlayer = mulliganState.startingPlayer)
)

private fun GameState.requestRandomizationForPlayersToShuffleDecks(): GameStatePendingRandomization =
    pendingRandomization(
        PendingRandomization(
            actionOnResolution = PerformMulligans,
            request = RandomRequest(
                shuffles = players.filter(mulled).map(PlayerState::library)
            )
        )
    )

private val kept = { it: PlayerState -> it.mulliganDecision == MulliganDecision.KEEP }
private val mulled = { it: PlayerState -> it.mulliganDecision == MulliganDecision.MULLIGAN }
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

