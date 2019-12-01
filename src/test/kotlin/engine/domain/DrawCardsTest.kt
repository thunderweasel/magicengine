package engine.domain

import assertk.assertThat
import assertk.assertions.containsOnly
import engine.factories.PlayerStateFactory
import engine.model.Card.KnownCard
import org.junit.jupiter.api.Test

class DrawCardsTest {
    @Test
    fun `puts the correct number of cards in the hand and removes from the library`() {
        val newPlayerState = PlayerStateFactory.create(
            library = listOf("1", "2", "3", "4", "5").map { KnownCard(it) }
        ).drawCards(3)

        assertThat(newPlayerState.hand).containsOnly(
            KnownCard("1"), KnownCard("2"), KnownCard("3")
        )

        assertThat(newPlayerState.library).containsOnly(
            KnownCard("4"), KnownCard("5")
        )
    }
}
