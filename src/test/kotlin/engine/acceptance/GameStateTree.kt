package engine.acceptance

import engine.acceptance.GameStateTree.Edge.PlayerChoice
import engine.acceptance.GameStateTree.Edge.Possibility
import engine.action.PlayerAction
import engine.action.ResolvedRandomization
import engine.model.GameState

object GameStateTree {
    sealed class OutcomeNode {
        abstract val description: String?
        abstract val edges: List<Edge>
        data class Resolved(
            override val description: String? = null,
            val state: GameState,
            val choices: List<PlayerChoice> = emptyList()
        ) : OutcomeNode() {
            override val edges get() = choices
        }

        data class PendingRandomization(
            override val description: String? = null,
            val possibilities: List<Possibility>
        ) : OutcomeNode() {
            override val edges get() = possibilities
        }

        data class CommandFailure(
            override val description: String? = null,
            val error: Throwable
        ) : OutcomeNode() {
            override val edges = emptyList<Edge>()
        }
    }

    sealed class Edge {
        abstract val expectedOutcome: OutcomeNode
        abstract val description: String?
        data class PlayerChoice(
            override val description: String? = null,
            val action: PlayerAction,
            override val expectedOutcome: OutcomeNode
        ) : Edge()

        data class Possibility(
            override val description: String? = null,
            val action: ResolvedRandomization,
            override val expectedOutcome: OutcomeNode
        ) : Edge()
    }
}
