package engine

import engine.action.ChooseFirstPlayer
import engine.factories.DeckFactory
import engine.factories.PlayerStateFactory
import engine.model.ResolvingMulligans
import engine.model.StartingPlayerMustBeChosen
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MagicEngineTest {
    private val sut = MagicEngine()

    @Test
    fun `can start game and perform actions`() {
        val startGameState = sut.start2PlayerGame(DeckFactory.alice, DeckFactory.bob)
        assertThat(startGameState.gameStart).isInstanceOf(StartingPlayerMustBeChosen::class.java)
        val gameStart = startGameState.gameStart as StartingPlayerMustBeChosen
        assertThat(gameStart.player).isNotNull()
        val winnerOfCoinToss = gameStart.player!!

        val newState = sut.performAction(
            action = ChooseFirstPlayer(
                actingPlayer = winnerOfCoinToss,
                chosenPlayer = PlayerStateFactory.ID_ALICE
            ), state = startGameState
        )
        assertThat(newState.gameStart).isInstanceOf(ResolvingMulligans::class.java)
    }
}
