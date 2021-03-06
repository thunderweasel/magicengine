package engine.reducer

import engine.action.PlayLand
import engine.domain.createPermanent
import engine.formats.MagicFormat
import engine.state.Card
import engine.state.GameState
import engine.state.InvalidPlayerAction
import engine.state.PostCombatMainPhase
import engine.state.PreCombatMainPhase
import engine.state.noPendingRandomization

fun playLandsReducer(format: MagicFormat): GameStatePendingRandomizationReducer = { state, action ->
    when (action) {
        is PlayLand -> playLand(state.gameState, action, format).noPendingRandomization()
        else -> state // not handled by this reducer
    }
}

private fun playLand(state: GameState, action: PlayLand, format: MagicFormat): GameState {
    val turn = mustBeActivePlayerWithPriority(state, action)
    val card = state.player(action.actingPlayer).hand.find { it.id == action.cardId }
        ?: throw InvalidPlayerAction(
            action = action,
            state = state,
            reason = "Card ID ${action.cardId} is not in player ${action.actingPlayer}'s hand"
        )
    if (card !is Card.KnownCard) {
        throw InvalidPlayerAction(
            action = action,
            state = state,
            reason = "Cannot play unknown card"
        )
    }
    val cardSpec = format.cardLookup[card.name]
    require(cardSpec != null) { "Card is not valid in this format: $card" }
    if (turn.history.numberOfLandsPlayed > 0) {
        throw InvalidPlayerAction(
            action = action,
            state = state,
            reason = "Player ${action.actingPlayer} cannot play another land this turn"
        )
    }
    if (turn.phase !in setOf(PreCombatMainPhase, PostCombatMainPhase)) {
        throw InvalidPlayerAction(
            action = action,
            state = state,
            reason = "Lands can only be played during the active player's main phase"
        )
    }
    return state
        .createPermanent(
            card = card,
            cardSpec = cardSpec,
            controller = action.actingPlayer
        )
        .copy(
            players = state.replacePlayerState(action.actingPlayer) {
                copy(
                    hand = hand.filter { it.id != action.cardId }
                )
            },
            temporalPosition = turn.copy(
                history = turn.history.copy(
                    numberOfLandsPlayed = turn.history.numberOfLandsPlayed + 1
                )
            )
        )
}
