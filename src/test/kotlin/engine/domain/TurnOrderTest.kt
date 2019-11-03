package engine.domain

import engine.factories.PlayerStateFactory
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TurnOrderTest : StringSpec({
    val players = listOf(
        PlayerStateFactory.create(1),
        PlayerStateFactory.create(3),
        PlayerStateFactory.create(5)
    )

    "if not at end of list, just go to next one" {
        nextInTurnOrder(1, players) shouldBe 3
        nextInTurnOrder(3, players) shouldBe 5
    }

    "if at end of list, loop back to beginning" {
        nextInTurnOrder(5, players) shouldBe 1
    }
})