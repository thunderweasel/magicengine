package engine.random

import kotlin.random.Random
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val SEED = 42
private const val MAX_VALUE = 2000000000

internal class ActualRandomizerTest {
    @Test
    fun `should always return the same thing when the same seed is provided`() {
        val randomNumber = ActualRandomizer(Random(SEED)).randomInt(0, MAX_VALUE)

        assertThat(ActualRandomizer(Random(SEED)).randomInt(0, MAX_VALUE)).isEqualTo(randomNumber)
        assertThat(ActualRandomizer(Random(SEED)).randomInt(0, MAX_VALUE)).isEqualTo(randomNumber)
        assertThat(ActualRandomizer(Random(SEED)).randomInt(0, MAX_VALUE)).isEqualTo(randomNumber)
        assertThat(ActualRandomizer(Random(SEED)).randomInt(0, MAX_VALUE)).isEqualTo(randomNumber)
        assertThat(ActualRandomizer(Random(SEED)).randomInt(0, MAX_VALUE)).isEqualTo(randomNumber)
    }
}
