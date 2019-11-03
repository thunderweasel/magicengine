package engine.model

sealed class TurnOrder {
    data class PlayerMustChoose(val playerId: Int) : TurnOrder()
    data class PlayerGoesFirst(val playerId: Int) : TurnOrder()
}
