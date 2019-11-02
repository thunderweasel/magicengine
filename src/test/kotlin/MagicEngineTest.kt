import io.kotlintest.matchers.instanceOf
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class MagicEngineTest : StringSpec({
    "should be able to initialize a two player game" {
        val alice = Player(name = "Alice")
        val bob = Player(name = "Bob")
        MagicEngine.start2PlayerGame(
            player1 = alice,
            player2 = bob
        ) shouldBe GameState(
            players = mapOf(1 to alice, 2 to bob)
        )
    }
})