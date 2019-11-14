package engine.domain

import engine.factories.PlayerStateFactory
import engine.model.Card.KnownCard
import engine.model.toCardId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DrawCardsTest {
    @Test
    fun `puts the correct number of cards in the hand and removes from the library`() {
        val newPlayerState = PlayerStateFactory.create(
            library = listOf("1", "2", "3", "4", "5").mapIndexed { i, name ->
                KnownCard(
                    name = name,
                    id = (i + 1).toCardId()
                )
            }
        ).drawCards(3)

        assertThat(newPlayerState.hand).containsExactlyInAnyOrder(
            KnownCard("1", 1), KnownCard("2", 2), KnownCard("3", 2)
        )

        assertThat(newPlayerState.library).containsExactlyInAnyOrder(
            KnownCard("4", 4), KnownCard("5", 5)
        )
    }
}