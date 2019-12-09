package engine.state

import assertk.assertThat
import assertk.assertions.isEqualTo
import engine.domain.ManaType
import org.junit.Test

internal class ManaPoolTest {
    @Test
    fun `mana pool creation works with all types provided`() {
        assertThat(
            createManaPool(
                ManaType.WHITE to 1,
                ManaType.BLUE to 2,
                ManaType.BLACK to 3,
                ManaType.RED to 4,
                ManaType.GREEN to 5,
                ManaType.COLORLESS to 6
            )
        ).isEqualTo(
            mapOf<ManaType, Long>(
                ManaType.WHITE to 1,
                ManaType.BLUE to 2,
                ManaType.BLACK to 3,
                ManaType.RED to 4,
                ManaType.GREEN to 5,
                ManaType.COLORLESS to 6
            )
        )
    }

    @Test
    fun `mana pool creation fills in 0s for unprovided types`() {
        assertThat(
            createManaPool(
                ManaType.WHITE to 1,
                ManaType.BLACK to 3,
                ManaType.GREEN to 5
            )
        ).isEqualTo(
            mapOf<ManaType, Long>(
                ManaType.WHITE to 1,
                ManaType.BLUE to 0,
                ManaType.BLACK to 3,
                ManaType.RED to 0,
                ManaType.GREEN to 5,
                ManaType.COLORLESS to 0
            )
        )
    }

    @Test
    fun `adding adds mana of the specified types`() {
        assertThat(
            createManaPool(
                ManaType.WHITE to 1,
                ManaType.BLUE to 2,
                ManaType.BLACK to 3,
                ManaType.RED to 4,
                ManaType.GREEN to 5,
                ManaType.COLORLESS to 6
            ).adding(
                ManaType.WHITE to 1L,
                ManaType.BLUE to 1L,
                ManaType.BLACK to 1L,
                ManaType.RED to 1L,
                ManaType.GREEN to 1L,
                ManaType.COLORLESS to 1L
            )
        ).isEqualTo(
            mapOf<ManaType, Long>(
                ManaType.WHITE to 2,
                ManaType.BLUE to 3,
                ManaType.BLACK to 4,
                ManaType.RED to 5,
                ManaType.GREEN to 6,
                ManaType.COLORLESS to 7
            )
        )
    }

    @Test
    fun `adding does not change unspecified values`() {
        assertThat(
            createManaPool(
                ManaType.WHITE to 1,
                ManaType.BLUE to 2,
                ManaType.BLACK to 3,
                ManaType.RED to 4,
                ManaType.GREEN to 5,
                ManaType.COLORLESS to 6
            ).adding(
                ManaType.WHITE to 1L,
                ManaType.BLACK to 1L,
                ManaType.GREEN to 1L
            )
        ).isEqualTo(
            mapOf<ManaType, Long>(
                ManaType.WHITE to 2,
                ManaType.BLUE to 2,
                ManaType.BLACK to 4,
                ManaType.RED to 4,
                ManaType.GREEN to 6,
                ManaType.COLORLESS to 6
            )
        )
    }
}
