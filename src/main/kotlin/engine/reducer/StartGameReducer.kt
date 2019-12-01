package engine.reducer

import engine.action.ChooseFirstPlayer
import engine.action.ChooseToKeepHand
import engine.action.ChooseToMulligan
import engine.action.ElectDeciderOfStartingPlayer
import engine.action.GameAction
import engine.action.PendingRandomization
import engine.action.PerformMulligans
import engine.action.PlayerAction
import engine.action.RandomizedResultAction
import engine.domain.drawCards
import engine.domain.firstInTurnOrder
import engine.domain.nextInTurnOrder
import engine.domain.startTurn
import engine.state.GameState
import engine.state.GameStatePendingRandomization
import engine.state.InvalidPlayerAction
import engine.state.MulliganDecision
import engine.state.PlayerId
import engine.state.PlayerState
import engine.state.RandomRequest
import engine.state.ResolvingMulligans
import engine.state.StartingPlayerMustBeChosen
import engine.state.noPendingRandomization
import engine.state.pendingRandomization

private const val STARTING_HAND_SIZE = 7

val gameStartReducer: GameStatePendingRandomizationReducer = { state, action ->
    when (state.gameState.temporalPosition) {
        is StartingPlayerMustBeChosen -> firstPlayerMustBeChosenStateReduce(state.gameState, action)
        is ResolvingMulligans -> mulliganStateReduce(state, action)
        else -> state // not handled by this reducer
    }
}

private fun firstPlayerMustBeChosenStateReduce(
    state: GameState,
    action: GameAction
): GameStatePendingRandomization {
    require(state.temporalPosition is StartingPlayerMustBeChosen)
    return when {
        action is RandomizedResultAction && action.innerAction == ElectDeciderOfStartingPlayer ->
            state.copy(
                players = state.players
                    .zip(action.resolvedRandomization.completedShuffles)
                    .map { (playerState, shuffledDeck) ->
                        playerState.copy(
                            library = shuffledDeck
                        )
                    },
                temporalPosition = StartingPlayerMustBeChosen(action.resolvedRandomization.generatedNumbers.first())
            )
        action is ChooseFirstPlayer -> {
            validateActingPlayer(state, action, state.temporalPosition.player!!)
            drawOpeningHands(state, action)
        }
        else -> throw actionDoesNotMatchState(state, action)
    }.noPendingRandomization()
}

private fun validateActingPlayer(
    state: GameState,
    action: PlayerAction,
    expectedActingPlayer: PlayerId
) {
    if (action.actingPlayer != expectedActingPlayer) {
        throw InvalidPlayerAction(
            action = action,
            state = state,
            reason = "Player ${action.actingPlayer} is acting, but should be player $expectedActingPlayer"
        )
    }
}

private fun drawOpeningHands(
    state: GameState,
    action: ChooseFirstPlayer
): GameState =
    state.copy(
        players = state.players.map { it.drawCards(STARTING_HAND_SIZE) },
        temporalPosition = ResolvingMulligans(
            numberOfMulligans = 0,
            startingPlayer = action.chosenPlayer,
            turnToDecide = action.chosenPlayer,
            mulliganDecisions = state.players
                .map { it.id to MulliganDecision.UNDECIDED }
                .toMap()
        )
    )

private fun mulliganStateReduce(
    state: GameStatePendingRandomization,
    action: GameAction
): GameStatePendingRandomization {
    val gameState = state.gameState
    val mulliganState = gameState.temporalPosition
    require(mulliganState is ResolvingMulligans)
    return when {
        action is ChooseToKeepHand -> {
            if (action.toBottom.size != mulliganState.numberOfMulligans) throw InvalidPlayerAction(
                action = action,
                state = gameState,
                reason = "toBottom should have size ${mulliganState.numberOfMulligans} but has size ${action.toBottom.size}"
            )
            gameState.makeDecision(MulliganDecision.KEEP)
                .putCardsOnBottom(playerId = mulliganState.turnToDecide, toBottom = action.toBottom)
                .checkIfAllPlayersDecidedMulligans()
        }
        action is ChooseToMulligan -> {
            gameState.makeDecision(MulliganDecision.MULLIGAN)
                .putHandBack(playerId = mulliganState.turnToDecide)
                .checkIfAllPlayersDecidedMulligans()
        }
        action is RandomizedResultAction && action.innerAction == PerformMulligans -> {
            gameState.performMulligans(action)
                .eachPlayerWhoMulledDecidesWhetherToKeepAgain()
                .noPendingRandomization()
        }
        else -> {
            throw actionDoesNotMatchState(state.gameState, action)
        }
    }
}

private fun actionDoesNotMatchState(
    state: GameState,
    action: GameAction
): InvalidPlayerAction {
    require(action is PlayerAction) { "Unhandled internal action!" }
    return InvalidPlayerAction(
        action = action,
        state = state,
        reason = "Action is invalid in current state"
    )
}

private fun GameState.makeDecision(
    mulliganDecision: MulliganDecision
): GameState {
    require(temporalPosition is ResolvingMulligans)
    return copy(
        temporalPosition = temporalPosition.copy(
            turnToDecide = nextPlayerAfterMulliganDecision(),
            mulliganDecisions = temporalPosition.mulliganDecisions.plus(temporalPosition.turnToDecide to mulliganDecision)
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
): GameState {
    require(temporalPosition is ResolvingMulligans)
    val playersWhoMulled = players.filter(temporalPosition.whoMulled())
    return copy(
        players = players.replacePlayerStates(
            action.resolvedRandomization.completedShuffles
                .zip(playersWhoMulled)
                .map { (completedShuffle, playerState) ->
                    playerState
                        .copy(
                            library = completedShuffle
                        )
                        .drawCards(STARTING_HAND_SIZE)
                }
        ),
        temporalPosition = temporalPosition.copy(
            mulliganDecisions = temporalPosition.mulliganDecisions.plus(
                playersWhoMulled.map { it.id to MulliganDecision.UNDECIDED }
            )
        )
    )
}

private fun GameState.eachPlayerWhoMulledDecidesWhetherToKeepAgain(): GameState {
    require(temporalPosition is ResolvingMulligans)
    return copy(
        temporalPosition = temporalPosition.copy(
            numberOfMulligans = temporalPosition.numberOfMulligans + 1,
            turnToDecide = firstInTurnOrder(
                startingPlayer = temporalPosition.startingPlayer,
                players = players,
                filter = temporalPosition.whoAreUndecided()
            ) ?: throw IllegalStateException("Unexpected state: no players undecided after performing mulligans")
        )
    )
}

private fun GameState.nextPlayerAfterMulliganDecision(): PlayerId {
    require(temporalPosition is ResolvingMulligans)
    return nextInTurnOrder(
        current = temporalPosition.turnToDecide,
        players = players,
        filter = temporalPosition.whoAreUndecided()
    ) ?: temporalPosition.startingPlayer
}

private fun GameState.checkIfAllPlayersDecidedMulligans(): GameStatePendingRandomization {
    require(temporalPosition is ResolvingMulligans)
    return when {
        players.all(temporalPosition.whoKept()) ->
            startTheGame().noPendingRandomization()
        players.any(temporalPosition.whoAreUndecided()) ->
            // do nothing
            this.noPendingRandomization()
        else ->
            requestRandomizationForPlayersToShuffleDecks(temporalPosition)
    }
}

private fun GameState.startTheGame(): GameState {
    require(temporalPosition is ResolvingMulligans)
    return startTurn(
        activePlayer = temporalPosition.startingPlayer,
        firstTurn = true
    )
}

private fun GameState.requestRandomizationForPlayersToShuffleDecks(mulliganState: ResolvingMulligans): GameStatePendingRandomization =
    pendingRandomization(
        PendingRandomization(
            actionOnResolution = PerformMulligans,
            request = RandomRequest(
                shuffles = players.filter(mulliganState.whoMulled()).map(PlayerState::library)
            )
        )
    )

private fun ResolvingMulligans.whoKept() = checkMulliganDecision(MulliganDecision.KEEP)
private fun ResolvingMulligans.whoMulled() = checkMulliganDecision(MulliganDecision.MULLIGAN)
private fun ResolvingMulligans.whoAreUndecided() = checkMulliganDecision(MulliganDecision.UNDECIDED)
private fun ResolvingMulligans.checkMulliganDecision(decision: MulliganDecision) = { it: PlayerState -> mulliganDecisions[it.id] == decision }
