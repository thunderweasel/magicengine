package engine

import assertk.assertThat
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import engine.action.ChooseFirstPlayer
import engine.factories.DeckFactory
import engine.factories.PlayerStateFactory
import engine.formats.EverythingIsAForest
import engine.state.ResolvingMulligans
import engine.state.StartingPlayerMustBeChosen
import org.junit.jupiter.api.Test

internal class MagicEngineTest {
    private val sut = MagicEngine(format = EverythingIsAForest())

    @Test
    fun `can start game and perform actions`() {
        val startGameState = sut.start2PlayerGame(DeckFactory.alice, DeckFactory.bob)
        assertThat(startGameState.temporalPosition)
            .isInstanceOf(StartingPlayerMustBeChosen::class.java)
            .given { gameStart ->
                assertThat(gameStart.player).isNotNull()

                val winnerOfCoinToss = gameStart.player!!
                val newState = sut.performAction(
                    action = ChooseFirstPlayer(
                        actingPlayer = winnerOfCoinToss,
                        chosenPlayer = PlayerStateFactory.ID_ALICE
                    ), state = startGameState
                )
                assertThat(newState.temporalPosition).isInstanceOf(ResolvingMulligans::class.java)
            }
    }
}
