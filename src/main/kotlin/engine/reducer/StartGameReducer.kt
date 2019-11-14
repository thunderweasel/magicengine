package engine.reducer

import engine.action.GameAction
import engine.action.PendingRandomization
import engine.action.PlayerAction
import engine.action.RandomizedResultAction
import engine.action.RandomizedResultAction.InnerAction.PerformMulligans
import engine.action.RandomizedResultAction.InnerAction.RandomizeChoiceForFirst
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
    when (state.gameState.gameStart) {
        is StartingPlayerMustBeChosen -> firstPlayerMustBeChosenStateReduce(action, state.gameState)
        is ResolvingMulligans -> mulliganStateReduce(action, state)
        else -> state
    }
}

private fun firstPlayerMustBeChosenStateReduce(
    action: GameAction,
    state: GameState
): GameStatePendingRandomization =
    when {
        action is RandomizedResultAction && action.innerAction == RandomizeChoiceForFirst ->
            state.copy(
                players = state.players
                    .zip(action.resolvedRandomization.completedShuffles)
                    .map { (playerState, shuffledDeck) ->
                        playerState.copy(
                            library = shuffledDeck
                        )
                    },
                gameStart = StartingPlayerMustBeChosen(action.resolvedRandomization.generatedNumbers.first())
            )
        action is PlayerAction.ChooseFirstPlayer -> drawOpeningHands(state, action)
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
            turnToDecide = action.chosenPlayer,
            mulliganDecisions = state.players
                .map { it.id to MulliganDecision.UNDECIDED }
                .toMap()
        )
    )

private fun mulliganStateReduce(
    action: GameAction,
    state: GameStatePendingRandomization
): GameStatePendingRandomization {
    val gameState = state.gameState
    val mulliganState = gameState.gameStart
    require(mulliganState is ResolvingMulligans)
    return when {
        action is PlayerAction.KeepHand -> {
            if (action.toBottom.size != mulliganState.numberOfMulligans) throw InvalidPlayerAction(
                action = action,
                state = gameState,
                reason = "toBottom should have size ${mulliganState.numberOfMulligans} but has size ${action.toBottom.size}"
            )
            gameState.makeDecision(MulliganDecision.KEEP)
                .putCardsOnBottom(playerId = mulliganState.turnToDecide, toBottom = action.toBottom)
                .checkIfAllPlayersDecidedMulligans()
        }
        action is PlayerAction.Mulligan -> {
            gameState.makeDecision(MulliganDecision.MULLIGAN)
                .putHandBack(playerId = mulliganState.turnToDecide)
                .checkIfAllPlayersDecidedMulligans()
        }
        action is RandomizedResultAction && action.innerAction == PerformMulligans -> {
            gameState.performMulligans(action)
                .eachPlayerWhoMulledDecidesWhetherToKeepAgain()
                .noPendingRandomization()
        }
        else -> state
    }
}

private fun GameState.makeDecision(
    mulliganDecision: MulliganDecision
): GameState {
    require(gameStart is ResolvingMulligans)
    return copy(
        gameStart = gameStart.copy(
            turnToDecide = nextPlayerAfterMulliganDecision(),
            mulliganDecisions = gameStart.mulliganDecisions.plus(gameStart.turnToDecide to mulliganDecision)
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
    require(gameStart is ResolvingMulligans)
    val playersWhoMulled = players.filter(gameStart.whoMulled())
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
        gameStart = gameStart.copy(
            mulliganDecisions = gameStart.mulliganDecisions.plus(
                playersWhoMulled.map { it.id to MulliganDecision.UNDECIDED }
            )
        )
    )
}

private fun GameState.eachPlayerWhoMulledDecidesWhetherToKeepAgain(): GameState {
    require(gameStart is ResolvingMulligans)
    return copy(
        gameStart = gameStart.copy(
            numberOfMulligans = gameStart.numberOfMulligans + 1,
            turnToDecide = firstInTurnOrder(
                startingPlayer = gameStart.startingPlayer,
                players = players,
                filter = gameStart.whoAreUndecided()
            ) ?: throw IllegalStateException("Unexpected state: no players undecided after performing mulligans")
        )
    )
}

private fun GameState.nextPlayerAfterMulliganDecision(): PlayerId {
    require(gameStart is ResolvingMulligans)
    return nextInTurnOrder(
        current = gameStart.turnToDecide,
        players = players,
        filter = gameStart.whoAreUndecided()
    ) ?: gameStart.startingPlayer
}

private fun GameState.checkIfAllPlayersDecidedMulligans(): GameStatePendingRandomization {
    require(gameStart is ResolvingMulligans)
    return when {
        players.all(gameStart.whoKept()) ->
            startTheGame().noPendingRandomization()
        players.any(gameStart.whoAreUndecided()) ->
            // do nothing
            this.noPendingRandomization()
        else ->
            requestRandomizationForPlayersToShuffleDecks(gameStart)
    }
}

private fun GameState.startTheGame(): GameState {
    require(gameStart is ResolvingMulligans)
    return copy(
        gameStart = GameStart.GameStarted(startingPlayer = gameStart.startingPlayer)
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
