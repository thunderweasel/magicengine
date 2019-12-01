package engine.reducer

import engine.action.PlayLand
import engine.formats.MagicFormat
import engine.state.Card
import engine.state.GameState
import engine.state.InvalidPlayerAction
import engine.state.Permanent
import engine.state.noPendingRandomization

fun playLandsReducer(format: MagicFormat): GameStatePendingRandomizationReducer = { state, action ->
    when (action) {
        is PlayLand -> playLand(state.gameState, action, format).noPendingRandomization()
        else -> state // not handled by this reducer
    }
}

private fun playLand(state: GameState, action: PlayLand, format: MagicFormat): GameState {
    mustBeActivePlayerWithPriority(state, action)
    val card = state.player(action.actingPlayer).hand[action.indexInHand]
    if (card !is Card.KnownCard) {
        throw InvalidPlayerAction(
            action = action,
            state = state,
            reason = "Cannot play unknown card"
        )
    }
    val cardSpec = format.cardLookup[card.name]
    require(cardSpec != null) { "Card is not valid in this format: $card" }
    return state.copy(
        players = state.replacePlayerState(action.actingPlayer) {
            copy(
                hand = hand.toMutableList().apply {
                    removeAt(action.indexInHand)
                }
            )
        },
        battlefield = state.battlefield.plus(
            Permanent(
                name = cardSpec.name,
                cardType = cardSpec.cardType,
                subtype = cardSpec.subtype,
                card = card
            )
        )
    )
}
