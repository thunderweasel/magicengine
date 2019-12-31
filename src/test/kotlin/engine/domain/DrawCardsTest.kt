package engine.domain

import assertk.assertThat
import assertk.assertions.containsOnly
import engine.factories.PlayerStateFactory
import engine.state.Card.KnownCard
import org.junit.jupiter.api.Test

class DrawCardsTest {
    @Test
    fun `puts the correct number of cards in the hand and removes from the library`() {
        val newPlayerState = PlayerStateFactory.create(
            library = (1..5).toList().map { KnownCard(it, "$it") }
        ).drawCards(3)

        assertThat(newPlayerState.hand).containsOnly(
            KnownCard(1, "1"), KnownCard(2, "2"), KnownCard(3, "3")
        )

        assertThat(newPlayerState.library).containsOnly(
            KnownCard(4, "4"), KnownCard(5, "5")
        )
    }

    @Test
    fun `does not replace the cards already in the player's hand`() {
        val newPlayerState = PlayerStateFactory.create(
            hand = listOf("A", "B", "C").mapIndexed { index, name -> KnownCard(6 + index, name) },
            library = (1..5).toList().map { KnownCard(it, "$it") }
        ).drawCards(1)

        assertThat(newPlayerState.hand).containsOnly(
            KnownCard(6, "A"), KnownCard(7, "B"), KnownCard(8, "C"), KnownCard(1, "1")
        )
    }
}
