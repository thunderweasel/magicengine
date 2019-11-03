package engine.model

sealed class GameAction {
    data class ChooseFirstPlayer(val actingPlayer: Int, val chosenPlayer: Int): GameAction()
}