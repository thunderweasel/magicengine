package engine.action

import engine.model.RandomResult

sealed class GameAction {
    data class ByPlayer(val playerAction: PlayerAction): GameAction()
    data class RandomizationResult(val result: RandomResult): GameAction()
}