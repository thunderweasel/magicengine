package engine.action

sealed class GameAction {
    data class ChooseFirstPlayer(val chosenPlayer: Int): GameAction()
}