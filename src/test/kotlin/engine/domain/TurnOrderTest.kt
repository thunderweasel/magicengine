package engine.domain

import engine.factories.PlayerStateFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TurnOrderTest {
    private val players = listOf(
        PlayerStateFactory.create(1),
        PlayerStateFactory.create(3),
        PlayerStateFactory.create(5)
    )

    @Test
    fun `if not at end of list, just go to next one`() {
        assertThat(nextInTurnOrder(1, players)).isEqualTo(3)
        assertThat(nextInTurnOrder(3, players)).isEqualTo(5)
    }

    @Test
    fun `if at end of list, loop back to beginning`() {
        assertThat(nextInTurnOrder(5, players)).isEqualTo(1)
    }
}