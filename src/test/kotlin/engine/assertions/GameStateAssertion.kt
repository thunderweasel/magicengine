package engine.assertions

import engine.model.Card
import engine.model.GameState
import org.assertj.core.api.AbstractAssert

class GameStateAssertion(actual: GameState) :
    AbstractAssert<GameStateAssertion, GameState>(actual, GameState::class.java) {
    companion object {
        fun assertThat(actual: GameState): GameStateAssertion =
            GameStateAssertion(actual)
    }

    fun isEquivalentTo(other: GameState): GameStateAssertion {
        isValidState()

        if (!other.isValidState()) {
            failWithMessage("Right-side state is invalid: $other")
        }



        return this
    }

    fun isValidState(): GameStateAssertion {
        if (!actual.isValidState()) {
            failWithMessage("Expected state to be valid, but has duplicate card IDs: $actual")
        }

        return this
    }


    private fun attemptToMakeIdsTheSameAsActual(other: GameState): GameState {
        return other.copy(
            players = other.players.map {
                it.copy(
                    hand =
                )
            }
        )
    }

    private fun List<Card>.attemptToMakeIdsTheSameAs(other: List<Card>, failureMessage: String): List<Card> {
        val otherCopy = other.toMutableList()
        val altered = map { card ->
            
        }
    }
}
private fun GameState.isValidState(): Boolean {
    val allCardIds = players
        .map { it.hand.plus(it.library) }
        .flatten()
        .map { it.id }
    return allCardIds.size == allCardIds.toSet().size
}