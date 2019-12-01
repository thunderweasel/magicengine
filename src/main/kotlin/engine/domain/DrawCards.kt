package engine.domain

import engine.state.PlayerState

fun PlayerState.drawCards(number: Int): PlayerState =
    copy(
        hand = hand.plus(library.slice(0 until number)),
        library = library.slice(number until library.size)
    )
