package engine.domain

import engine.factories.PlayerStateFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TurnOrderTest {
    private val players = listOf(
        PlayerStateFactory.create(1),
        PlayerStateFactory.create(3),
        PlayerStateFactory.create(5)
    )

    @Nested
    inner class NextInTurnOrder {
        @Test
        fun `if not at end of list, just go to next one`() {
            assertThat(nextInTurnOrder(1, players)).isEqualTo(3)
            assertThat(nextInTurnOrder(3, players)).isEqualTo(5)
        }

        @Test
        fun `if at end of list, loop back to beginning`() {
            assertThat(nextInTurnOrder(5, players)).isEqualTo(1)
        }

        @Test
        fun `if the next player is filtered out, it skips to the next player`() {
            assertThat(nextInTurnOrder(1, players) { it.id != 3 }).isEqualTo(5)
        }

        @Test
        fun `if the starting player is filtered out, it still selects the next player`() {
            assertThat(nextInTurnOrder(1, players) { it.id != 1 }).isEqualTo(3)
        }

        @Test
        fun `if all players are filtered out, returns null`() {
            assertThat(nextInTurnOrder(1, players) { false }).isNull()
        }
    }

    @Nested
    inner class FirstInTurnOrder {
        @Test
        fun `if the starting player is not filtered, return that`() {
            assertThat(firstInTurnOrder(1, players) { true }).isEqualTo(1)
        }

        @Test
        fun `if the starting player is filtered, return the next unfiltered one`() {
            assertThat(firstInTurnOrder(1, players) { it.id != 1 }).isEqualTo(3)
            assertThat(firstInTurnOrder(1, players) { it.id == 5 }).isEqualTo(5)
            assertThat(firstInTurnOrder(3, players) { it.id == 1 }).isEqualTo(1)
            assertThat(firstInTurnOrder(5, players) { it.id == 3 }).isEqualTo(3)
        }
    }
}