package engine.model

import engine.action.PlayerAction

data class RandomResult(
    val action: PlayerAction,
    val results: List<Any>
)