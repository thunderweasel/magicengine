package engine.domain

import engine.factories.PlayerStateFactory
import engine.model.Card
import engine.model.card
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class DrawCardsTest {
    @Test
    fun `puts the correct number of cards in the hand and removes from the library`() {
        val newPlayerState = PlayerStateFactory.create(
            library = listOf("1", "2", "3", "4", "5").map { card(it) }
        ).drawCards(3)

        assertThat(newPlayerState.hand).containsExactlyInAnyOrder(
            card("1"), card("2"), card("3")
        )

        assertThat(newPlayerState.library).containsExactlyInAnyOrder(
            card("4"), card("5")
        )
    }
}