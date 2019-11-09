package engine.domain

import engine.factories.PlayerStateFactory
import engine.model.Card
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class DrawCardsTest {
    @Test
    fun `puts the correct number of cards in the hand and removes from the library`() {
        val newPlayerState = PlayerStateFactory.create(
            library = listOf("1", "2", "3", "4", "5").map { Card(it) }
        ).drawCards(3)

        assertThat(newPlayerState.hand).containsExactlyInAnyOrder(
            Card("1"), Card("2"), Card("3")
        )

        assertThat(newPlayerState.library).containsExactlyInAnyOrder(
            Card("4"), Card("5")
        )
    }
}