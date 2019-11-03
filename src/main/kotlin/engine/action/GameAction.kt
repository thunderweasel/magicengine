package engine.action

sealed class GameAction {
    data class ByPlayer(val playerAction: PlayerAction): GameAction()
    data class RandomizationResult(
        val playerAction: PlayerAction,
        val results: List<Any>
    ): GameAction()
}