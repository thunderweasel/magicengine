object MagicEngine {
    fun start2PlayerGame(player1: Player, player2: Player): GameState {
        return GameState(
            players = mapOf(1 to player1, 2 to player2)
        )
    }
}
