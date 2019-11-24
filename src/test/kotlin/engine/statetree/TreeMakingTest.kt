package engine.statetree

import engine.action.ChooseFirstPlayer
import engine.action.ChooseToKeepHand
import engine.action.ChooseToMulligan
import engine.action.ResolvedRandomization
import engine.factories.PlayerStateFactory
import engine.statetree.GameStateTree.Edge
import engine.statetree.GameStateTree.OutcomeNode
import engine.statetree.TreeMaking.Companion.makeStateTree
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TreeMakingTest {
    private data class MyState(val string: String)

    private val tree = makeStateTree<MyState> {
        pendingRandomization().thenBranch(
            "If random number is 1"(
                ResolvedRandomization(
                    generatedNumbers = listOf(1)
                ) resultsIn MyState("1")
                    .thenChain(
                        "A - choice"(
                            ChooseFirstPlayer(PlayerStateFactory.ID_ALICE) resultsIn MyState(
                                "1A"
                            )
                        ),
                        "B - choice resulting in randomization"(
                            ChooseToMulligan resultsIn pendingRandomization()
                        ),
                        "B - resolve random"(
                            ResolvedRandomization(
                                generatedNumbers = listOf(3)
                            ) resultsIn MyState("1B3")
                        ),
                        "C - Random branching"(
                            ChooseToMulligan resultsIn pendingRandomization()
                                .thenBranch(
                                    "is 5"(
                                        ResolvedRandomization(
                                            generatedNumbers = listOf(5)
                                        ) resultsIn MyState("1B3C5")
                                    ),
                                    "is 4"(
                                        ResolvedRandomization(
                                            generatedNumbers = listOf(4)
                                        ) resultsIn MyState("1B3C4")
                                    )
                                )
                        )
                    )
            ),
            "If random number is 2"(
                ResolvedRandomization(
                    generatedNumbers = listOf(2)
                ) resultsIn MyState("2")
                    .thenBranch(
                        "is D"(
                            ChooseToMulligan resultsIn MyState("2D")
                        ),
                        "is E"(
                            ChooseToKeepHand(emptyList()) resultsIn MyState(
                                "2E"
                            )
                        )
                    )
            )
        )
    }

    private val expectedTree: OutcomeNode<MyState> = OutcomeNode.PendingRandomization(
        possibilities = listOf(
            Edge.Possibility(
                "If random number is 1",
                action = ResolvedRandomization(
                    generatedNumbers = listOf(1)
                ),
                expectedOutcome = OutcomeNode.Resolved(
                    state = MyState("1"),
                    choices = listOf(
                        Edge.PlayerChoice(
                            description = "A - choice",
                            action = ChooseFirstPlayer(PlayerStateFactory.ID_ALICE),
                            expectedOutcome = OutcomeNode.Resolved(
                                state = MyState("1A"),
                                choices = listOf(
                                    Edge.PlayerChoice(
                                        description = "B - choice resulting in randomization",
                                        action = ChooseToMulligan,
                                        expectedOutcome = OutcomeNode.PendingRandomization(
                                            possibilities = listOf(
                                                Edge.Possibility(
                                                    description = "B - resolve random",
                                                    action = ResolvedRandomization(generatedNumbers = listOf(3)),
                                                    expectedOutcome = OutcomeNode.Resolved(
                                                        state = MyState(
                                                            "1B3"
                                                        ),
                                                        choices = listOf(
                                                            Edge.PlayerChoice(
                                                                description = "C - Random branching",
                                                                action = ChooseToMulligan,
                                                                expectedOutcome = OutcomeNode.PendingRandomization(
                                                                    possibilities = listOf(
                                                                        Edge.Possibility(
                                                                            description = "is 5",
                                                                            action = ResolvedRandomization(
                                                                                generatedNumbers = listOf(5)
                                                                            ),
                                                                            expectedOutcome = OutcomeNode.Resolved(
                                                                                state = MyState(
                                                                                    "1B3C5"
                                                                                )
                                                                            )
                                                                        ),
                                                                        Edge.Possibility(
                                                                            description = "is 4",
                                                                            action = ResolvedRandomization(
                                                                                generatedNumbers = listOf(4)
                                                                            ),
                                                                            expectedOutcome = OutcomeNode.Resolved(
                                                                                state = MyState(
                                                                                    "1B3C4"
                                                                                )
                                                                            )
                                                                        )
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            Edge.Possibility(
                "If random number is 2",
                action = ResolvedRandomization(
                    generatedNumbers = listOf(2)
                ),
                expectedOutcome = OutcomeNode.Resolved(
                    state = MyState("2"),
                    choices = listOf(
                        Edge.PlayerChoice(
                            description = "is D",
                            action = ChooseToMulligan,
                            expectedOutcome = OutcomeNode.Resolved(
                                state = MyState("2D")
                            )
                        ),
                        Edge.PlayerChoice(
                            description = "is E",
                            action = ChooseToKeepHand(emptyList()),
                            expectedOutcome = OutcomeNode.Resolved(
                                state = MyState("2E")
                            )
                        )
                    )
                )
            )
        )
    )

    @Test
    fun `the output of the convenience tree maker should be the same as the verbose, manual instantiation of the tree`() {
        assertThat(tree).isEqualTo(expectedTree)
    }
}
