package engine.acceptance

import engine.acceptance.GameStateTree.Edge
import engine.acceptance.GameStateTree.Edge.PlayerChoice
import engine.acceptance.GameStateTree.Edge.Possibility
import engine.acceptance.GameStateTree.OutcomeNode
import engine.acceptance.GameStateTree.OutcomeNode.CommandFailure
import engine.acceptance.TreeMaking.Companion.makeTree
import engine.action.ChooseFirstPlayer
import engine.action.ChooseToKeepHand
import engine.action.ChooseToMulligan
import engine.action.PlayerAction
import engine.action.ResolvedRandomization
import engine.factories.DeckFactory
import engine.factories.PlayerStateFactory
import engine.model.Card
import engine.model.GameStarted
import engine.model.GameState
import engine.model.InvalidPlayerAction
import engine.model.MulliganDecision
import engine.model.PlayerState
import engine.model.ResolvingMulligans
import engine.model.StartingPlayerMustBeChosen
import engine.random.CheatShuffler
import engine.random.ShuffleCheat

private val cheatShuffler = CheatShuffler<Card>(ShuffleCheat.MoveOneCardToBottom)
private fun List<Card>.shuffle(times: Int = 1) = (1..times).fold(this) { cards, _ ->
    cheatShuffler.shuffle(cards)
}

private object TreeStates {
    // Hands we expect Alice and Bob to draw due to above shuffle cheating
    val expectedAliceHand1 = DeckFactory.alice.slice(1..7)
    val expectedAliceHand2 = DeckFactory.alice.slice(2..8)
    val expectedAliceHand3 = DeckFactory.alice.slice(3..9)
    val expectedBobHand1 = DeckFactory.bob.slice(1..7)
    val expectedBobHand2 = DeckFactory.bob.slice(2..8)

    val aliceWinsCoinToss by lazy {
        GameState(
            players = listOf(
                PlayerState(
                    id = PlayerStateFactory.ID_ALICE,
                    library = DeckFactory.alice.shuffle(1),
                    lifeTotal = 20
                ),
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    library = DeckFactory.bob.shuffle(1),
                    lifeTotal = 20
                )
            ),
            gameStart = StartingPlayerMustBeChosen(PlayerStateFactory.ID_ALICE)
        )
    }
    val bobWinsCoinToss by lazy {
        GameState(
            players = listOf(
                PlayerState(
                    id = PlayerStateFactory.ID_ALICE,
                    library = DeckFactory.alice.shuffle(1),
                    lifeTotal = 20
                ),
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    library = DeckFactory.bob.shuffle(1),
                    lifeTotal = 20
                )
            ),
            gameStart = StartingPlayerMustBeChosen(PlayerStateFactory.ID_BOB)
        )
    }

    val drawnFirstHands by lazy {
        GameState(
            players = listOf(
                PlayerState(
                    id = PlayerStateFactory.ID_ALICE,
                    library = DeckFactory.alice.shuffle(1).minus(elements = expectedAliceHand1),
                    lifeTotal = 20,
                    hand = expectedAliceHand1
                ),
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    library = DeckFactory.bob.shuffle(1).minus(elements = expectedBobHand1),
                    lifeTotal = 20,
                    hand = expectedBobHand1
                )
            ),
            gameStart = ResolvingMulligans(
                numberOfMulligans = 0,
                startingPlayer = PlayerStateFactory.ID_BOB,
                turnToDecide = PlayerStateFactory.ID_BOB,
                mulliganDecisions = mapOf(
                    PlayerStateFactory.ID_ALICE to MulliganDecision.UNDECIDED,
                    PlayerStateFactory.ID_BOB to MulliganDecision.UNDECIDED
                )
            )
        )
    }

    val bobDecidedToTakeFirstMulligan by lazy {
        GameState(
            players = listOf(
                // Alice is unchanged
                drawnFirstHands.players[0],
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    library = DeckFactory.bob.shuffle(1),
                    lifeTotal = 20,
                    hand = emptyList()
                )
            ),
            gameStart = ResolvingMulligans(
                numberOfMulligans = 0,
                startingPlayer = PlayerStateFactory.ID_BOB,
                turnToDecide = PlayerStateFactory.ID_ALICE,
                mulliganDecisions = mapOf(
                    PlayerStateFactory.ID_ALICE to MulliganDecision.UNDECIDED,
                    PlayerStateFactory.ID_BOB to MulliganDecision.MULLIGAN
                )
            )
        )
    }

    val bothPlayersTookFirstMulligan by lazy {
        GameState(
            players = listOf(
                PlayerState(
                    id = PlayerStateFactory.ID_ALICE,
                    lifeTotal = 20,
                    // Should have drawn their second hand
                    hand = expectedAliceHand2,
                    // Library should have been shuffled twice now
                    library = DeckFactory.alice.shuffle(2).minus(elements = expectedAliceHand2)
                ),
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    lifeTotal = 20,
                    hand = expectedBobHand2,
                    library = DeckFactory.bob.shuffle(2).minus(elements = expectedBobHand2)
                )
            ),
            gameStart = ResolvingMulligans(
                numberOfMulligans = 1,
                startingPlayer = PlayerStateFactory.ID_BOB,
                turnToDecide = PlayerStateFactory.ID_BOB,
                mulliganDecisions = mapOf(
                    // Both go back to undecided, since they have to decide whether to keep new hand
                    PlayerStateFactory.ID_ALICE to MulliganDecision.UNDECIDED,
                    PlayerStateFactory.ID_BOB to MulliganDecision.UNDECIDED
                )
            )
        )
    }

    val bobDecidedToKeepAfterFirstMulligan by lazy {
        GameState(
            players = listOf(
                // Alice is unchanged
                bothPlayersTookFirstMulligan.players[0],
                PlayerState(
                    id = PlayerStateFactory.ID_BOB,
                    lifeTotal = 20,
                    // Bob put the card at index 3 to the bottom
                    hand = expectedBobHand2.minus(expectedBobHand2[3]),
                    library = DeckFactory.bob
                        .shuffle(2)
                        .minus(expectedBobHand2)
                        .plus(expectedBobHand2[3])
                )
            ),
            gameStart = ResolvingMulligans(
                numberOfMulligans = 1,
                startingPlayer = PlayerStateFactory.ID_BOB,
                turnToDecide = PlayerStateFactory.ID_ALICE,
                mulliganDecisions = mapOf(
                    PlayerStateFactory.ID_ALICE to MulliganDecision.UNDECIDED,
                    PlayerStateFactory.ID_BOB to MulliganDecision.KEEP
                )
            )
        )
    }

    val aliceTookSecondMulligan by lazy {
        GameState(
            players = listOf(
                PlayerState(
                    id = PlayerStateFactory.ID_ALICE,
                    lifeTotal = 20,
                    // Alice draws her third hand
                    hand = expectedAliceHand3,
                    library = DeckFactory.alice.shuffle(3).minus(expectedAliceHand3)
                ),
                // Bob is unchanged
                bobDecidedToKeepAfterFirstMulligan.players[1]
            ),
            gameStart = ResolvingMulligans(
                numberOfMulligans = 2,
                startingPlayer = PlayerStateFactory.ID_BOB,
                turnToDecide = PlayerStateFactory.ID_ALICE,
                mulliganDecisions = mapOf(
                    PlayerStateFactory.ID_ALICE to MulliganDecision.UNDECIDED,
                    PlayerStateFactory.ID_BOB to MulliganDecision.KEEP
                )
            )
        )
    }

    val mulligansResolved by lazy {
        GameState(
            players = listOf(
                PlayerState(
                    id = PlayerStateFactory.ID_ALICE,
                    lifeTotal = 20,
                    // Alice put the cards at index 4 and 5 to the bottom
                    hand = expectedAliceHand3.minus(expectedAliceHand3.slice(4..5)),
                    library = DeckFactory.alice
                        .shuffle(3)
                        .minus(elements = expectedAliceHand3)
                        .plus(expectedAliceHand3.slice(4..5))
                ),
                // Bob is unchanged
                aliceTookSecondMulligan.players[1]
            ),
            gameStart = GameStarted(startingPlayer = PlayerStateFactory.ID_BOB)
        )
    }
}

val startingTheGameTree =
    makeTree {
        "At the start of the game, decks are shuffled and a random player gets to choose turn order."(
            "If Alice wins the coin flip, she gets to choose the starting player."(
                ResolvedRandomization(
                    listOf(DeckFactory.alice.shuffle(), DeckFactory.bob.shuffle()),
                    listOf(PlayerStateFactory.ID_ALICE)
                ) resultsIn
                    TreeStates.aliceWinsCoinToss
                        .then(
                            chain(
                                "Once Alice chooses a starting player, all players draw their hands."(
                                    ChooseFirstPlayer(PlayerStateFactory.ID_BOB) resultsIn TreeStates.drawnFirstHands
                                ),
                                "The starting player decides whether to keep or mulligan first."(
                                    ChooseToMulligan resultsIn TreeStates.bobDecidedToTakeFirstMulligan
                                ),
                                "Then the next player chooses whether to mulligan, after which both players will mulligan simultaneously."(
                                    ChooseToMulligan resultsIn pendingRandomization()
                                ),
                                "Each player who chose to mulligan will draw a new hand of 7."(
                                    ResolvedRandomization(
                                        listOf(DeckFactory.alice.shuffle(2), DeckFactory.bob.shuffle(2))
                                    ) resultsIn TreeStates.bothPlayersTookFirstMulligan
                                ),
                                "Bob decides to keep after first mulligan"(
                                    ChooseToKeepHand(toBottom = listOf(3)) // 4th card to the bottom
                                        resultsIn TreeStates.bobDecidedToKeepAfterFirstMulligan
                                ),
                                "Alice chooses to mulligan again"(
                                    ChooseToMulligan resultsIn pendingRandomization()
                                ),
                                "Alice performs her 2nd mulligan"(
                                    ResolvedRandomization(
                                        listOf(DeckFactory.alice.shuffle(3))
                                    ) resultsIn TreeStates.aliceTookSecondMulligan
                                        .then(
                                            "If Alex keeps and puts the correct number of cards on the bottom, the game starts"(
                                                ChooseToKeepHand(
                                                    toBottom = listOf(
                                                        4,
                                                        5
                                                    )
                                                ) // 5th and 6th card to the bottom
                                                    resultsIn TreeStates.mulligansResolved
                                            ),
                                            "If Alex puts the wrong number of cards on the bottom, throw an error"(
                                                ChooseToKeepHand(toBottom = listOf(1, 4, 5))
                                                    resultsIn CommandFailure(
                                                    error = InvalidPlayerAction(
                                                        action = ChooseToKeepHand(toBottom = listOf(1, 4, 5)),
                                                        state = TreeStates.aliceTookSecondMulligan,
                                                        reason = "toBottom should have size 2 but has size 3"
                                                    )
                                                )
                                            )
                                        )
                                )
                            )
                        )
            ),
            "If Bob wins the coin flip, he gets to choose the starting player."(
                ResolvedRandomization(
                    listOf(DeckFactory.alice.shuffle(), DeckFactory.bob.shuffle()),
                    listOf(PlayerStateFactory.ID_BOB)
                ) resultsIn TreeStates.bobWinsCoinToss
            )
        )
    }

class TreeMaking private constructor() {
    companion object {
        fun makeTree(makeRoot: TreeMaking.() -> OutcomeNode) = TreeMaking().makeRoot()
    }

    fun GameState.then(vararg choices: PlayerChoice): OutcomeNode.Resolved =
        OutcomeNode.Resolved(state = this, choices = choices.toList())

    fun OutcomeNode.Resolved.then(vararg choices: PlayerChoice): OutcomeNode.Resolved =
        OutcomeNode.Resolved(state = state, choices = choices.toList())

    operator fun String.invoke(vararg possibilities: Possibility): OutcomeNode.PendingRandomization =
        OutcomeNode.PendingRandomization(description = this, possibilities = possibilities.toList())

    infix fun PlayerAction.resultsIn(outcome: OutcomeNode) =
        PlayerChoice(action = this, expectedOutcome = outcome)

    infix fun PlayerAction.resultsIn(state: GameState) =
        PlayerChoice(action = this, expectedOutcome = OutcomeNode.Resolved(state = state))

    infix fun ResolvedRandomization.resultsIn(outcome: OutcomeNode) =
        Possibility(action = this, expectedOutcome = outcome)

    infix fun ResolvedRandomization.resultsIn(state: GameState) =
        Possibility(
            action = this,
            expectedOutcome = OutcomeNode.Resolved(state = state)
        )

    operator fun String.invoke(choice: PlayerChoice): PlayerChoice =
        choice.copy(description = this)

    operator fun String.invoke(possibility: Possibility): Possibility =
        possibility.copy(description = this)

    fun pendingRandomization(vararg possibilities: Possibility): OutcomeNode.PendingRandomization =
        OutcomeNode.PendingRandomization(possibilities = possibilities.toList())

    inline fun <reified T : Edge> chain(vararg edges: Edge): T {
        require(edges.isNotEmpty())
        val firstEdge = edges.reduceRight(::chainTwo)
        require(firstEdge is T)
        return firstEdge
    }

    inline fun <reified T : Edge> chainTwo(current: Edge, prev: Edge): T {
        val thisOutcome = prev.expectedOutcome
        return when (current) {
            is PlayerChoice -> {
                require(thisOutcome is OutcomeNode.Resolved)
                prev.withNewOutcome(
                    thisOutcome.copy(
                        choices = thisOutcome.choices.plus(current)
                    )
                )
            }
            is Possibility -> {
                require(thisOutcome is OutcomeNode.PendingRandomization)
                prev.withNewOutcome(
                    thisOutcome.copy(
                        possibilities = thisOutcome.possibilities.plus(current)
                    )
                )
            }
        }
    }

    inline fun <reified T : Edge> Edge.withNewOutcome(outcome: OutcomeNode): T =
        when (this) {
            is PlayerChoice -> copy(expectedOutcome = outcome)
            is Possibility -> copy(expectedOutcome = outcome)
        } as T
}
