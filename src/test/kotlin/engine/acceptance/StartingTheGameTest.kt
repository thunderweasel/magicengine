package engine.acceptance

import engine.action.ChooseFirstPlayer
import engine.action.ChooseToKeepHand
import engine.action.ChooseToMulligan
import engine.action.ResolvedRandomization
import engine.domain.startingState
import engine.factories.DeckFactory
import engine.factories.GameStateFactory
import engine.factories.PlayerStateFactory.ID_ALICE
import engine.factories.PlayerStateFactory.ID_BOB
import engine.formats.AllSpellsAreBurnSpells
import engine.reducer.masterReducer
import engine.state.BeginningPhase
import engine.state.GameState
import engine.state.InvalidPlayerAction
import engine.state.MulliganDecision
import engine.state.PlayerState
import engine.state.ResolvingMulligans
import engine.state.StartingPlayerMustBeChosen
import engine.state.Turn
import engine.state.UpkeepStep
import engine.statetree.StateTreeTest
import engine.statetree.TreeMaking.Companion.makeStateTree
import org.junit.jupiter.api.DisplayName

// Every time we would shuffle in this test, we just move the top card to the bottom
private fun <T> List<T>.shuffle(times: Int = 1) = (1..times).fold(this) { cards, _ ->
    cards.drop(1).plus(cards[0])
}
private fun completedShuffles(numberOfDecks: Int) =
    (1..numberOfDecks).map {
        (0..59).toList().shuffle(times = 1)
    }

@DisplayName("103. Starting the Game")
class StartingTheGameTest : StateTreeTest<GameState>(
    reducer = masterReducer(format = AllSpellsAreBurnSpells()),
    root =
    makeStateTree {
        pendingRandomization(
            state = startingState(
                playerDecks = listOf(DeckFactory.aliceCardNames, DeckFactory.bobCardNames)
            )
        ).thenBranch(
            "If Alice wins the coin flip, she gets to choose the starting player."(
                ResolvedRandomization(
                    completedShuffles(numberOfDecks = 2),
                    listOf(ID_ALICE)
                ) resultsIn MulliganStates.aliceWinsCoinToss
                    .thenChain(
                        "Once Alice chooses a starting player, all players draw their hands."(
                            // Alice chooses to be on the draw
                            ChooseFirstPlayer(ID_ALICE, chosenPlayer = ID_BOB)
                                resultsIn MulliganStates.drawnFirstHands
                        ),
                        "The starting player decides whether to keep or mulligan first."(
                            ChooseToMulligan(ID_BOB) resultsIn MulliganStates.bobDecidedToTakeFirstMulligan
                        ),
                        "Then the next player chooses whether to mulligan, after which both players will mulligan simultaneously."(
                            ChooseToMulligan(ID_ALICE) resultsIn pendingRandomization()
                        ),
                        "Each player who chose to mulligan will draw a new hand of 7."(
                            ResolvedRandomization(
                                completedShuffles(numberOfDecks = 2)
                            ) resultsIn MulliganStates.bothPlayersTookFirstMulligan
                        ),
                        "Bob decides to keep after first mulligan"(
                            // 4th card to the bottom
                            ChooseToKeepHand(ID_BOB, toBottom = listOf(3))
                                resultsIn MulliganStates.bobDecidedToKeepAfterFirstMulligan
                        ),
                        "Alice chooses to mulligan again"(
                            ChooseToMulligan(ID_ALICE) resultsIn pendingRandomization()
                        ),
                        "Alice performs her 2nd mulligan"(
                            ResolvedRandomization(
                                completedShuffles(numberOfDecks = 1)
                            ) resultsIn MulliganStates.aliceTookSecondMulligan
                                .thenBranch(
                                    "If Alice keeps and puts the correct number of cards on the bottom, the game starts"(
                                        ChooseToKeepHand(
                                            ID_ALICE,
                                            toBottom = listOf(
                                                4,
                                                5
                                            )
                                        ) // 5th and 6th card to the bottom
                                            resultsIn MulliganStates.gameStarted
                                    ),
                                    "If Alice puts the wrong number of cards on the bottom, return invalid"(
                                        ChooseToKeepHand(ID_ALICE, toBottom = listOf(1, 4, 5))
                                            resultsIn InvalidPlayerAction(
                                            action = ChooseToKeepHand(ID_ALICE, toBottom = listOf(1, 4, 5)),
                                            state = MulliganStates.aliceTookSecondMulligan,
                                            reason = "toBottom should have size 2 but has size 3"
                                        )
                                    )
                                )
                        )
                    )
            ),
            "If Bob wins the coin flip, he gets to choose the starting player."(
                ResolvedRandomization(
                    listOf((0..59).toList().shuffle(), (0..59).toList().shuffle()),
                    listOf(ID_BOB)
                ) resultsIn MulliganStates.bobWinsCoinToss
                    .thenBranch(
                        "If Alice tries to choose the starting player, return invalid"(
                            ChooseFirstPlayer(ID_ALICE, chosenPlayer = ID_ALICE) resultsIn
                                InvalidPlayerAction(
                                    action = ChooseFirstPlayer(ID_ALICE, chosenPlayer = ID_ALICE),
                                    state = MulliganStates.bobWinsCoinToss,
                                    reason = "Player 1 is acting, but should be player 2"
                                )
                        ),
                        "Bob chooses the starting player"(
                            ChooseFirstPlayer(ID_BOB, chosenPlayer = ID_BOB) resultsIn MulliganStates.drawnFirstHands
                                .thenBranch(
                                    "If Bob tries to choose the starting player again, return invalid"(
                                        ChooseFirstPlayer(ID_BOB, chosenPlayer = ID_BOB) resultsIn
                                            InvalidPlayerAction.invalidTemporalState(
                                                action = ChooseFirstPlayer(ID_BOB, chosenPlayer = ID_BOB),
                                                state = MulliganStates.drawnFirstHands
                                            )
                                    )
                                )
                        ),
                        "If Bob tries to do something other than choose a starting player, return invalid"(
                            ChooseToMulligan(ID_BOB) resultsIn
                                InvalidPlayerAction.invalidTemporalState(
                                    action = ChooseToMulligan(ID_BOB),
                                    state = MulliganStates.bobWinsCoinToss
                                )
                        )
                    )
            )
        )
    }
)

private object MulliganStates {
    // Hands we expect Alice and Bob to draw due to cheat shuffling above
    val expectedAliceHand1 = DeckFactory.alice.slice(1..7)
    val expectedAliceHand2 = DeckFactory.alice.slice(2..8)
    val expectedAliceHand3 = DeckFactory.alice.slice(3..9)
    val expectedBobHand1 = DeckFactory.bob.slice(1..7)
    val expectedBobHand2 = DeckFactory.bob.slice(2..8)

    val aliceWinsCoinToss by lazy {
        GameStateFactory.create(
            players = listOf(
                PlayerState(
                    id = ID_ALICE,
                    library = DeckFactory.alice.shuffle(1),
                    lifeTotal = 20
                ),
                PlayerState(
                    id = ID_BOB,
                    library = DeckFactory.bob.shuffle(1),
                    lifeTotal = 20
                )
            ),
            temporalPosition = StartingPlayerMustBeChosen(ID_ALICE),
            nextCardId = 121,
            nextPermanentId = 1
        )
    }

    val bobWinsCoinToss by lazy {
        GameStateFactory.create(
            players = listOf(
                PlayerState(
                    id = ID_ALICE,
                    library = DeckFactory.alice.shuffle(1),
                    lifeTotal = 20
                ),
                PlayerState(
                    id = ID_BOB,
                    library = DeckFactory.bob.shuffle(1),
                    lifeTotal = 20
                )
            ),
            temporalPosition = StartingPlayerMustBeChosen(ID_BOB)
        )
    }

    val drawnFirstHands by lazy {
        GameStateFactory.create(
            players = listOf(
                PlayerState(
                    id = ID_ALICE,
                    library = DeckFactory.alice.shuffle(1).minus(elements = expectedAliceHand1),
                    lifeTotal = 20,
                    hand = expectedAliceHand1
                ),
                PlayerState(
                    id = ID_BOB,
                    library = DeckFactory.bob.shuffle(1).minus(elements = expectedBobHand1),
                    lifeTotal = 20,
                    hand = expectedBobHand1
                )
            ),
            temporalPosition = ResolvingMulligans(
                numberOfMulligans = 0,
                startingPlayer = ID_BOB,
                turnToDecide = ID_BOB,
                mulliganDecisions = mapOf(
                    ID_ALICE to MulliganDecision.UNDECIDED,
                    ID_BOB to MulliganDecision.UNDECIDED
                )
            )
        )
    }

    val bobDecidedToTakeFirstMulligan by lazy {
        GameStateFactory.create(
            players = listOf(
                // Alice is unchanged
                drawnFirstHands.players[0],
                PlayerState(
                    id = ID_BOB,
                    library = DeckFactory.bob.shuffle(1),
                    lifeTotal = 20,
                    hand = emptyList()
                )
            ),
            temporalPosition = ResolvingMulligans(
                numberOfMulligans = 0,
                startingPlayer = ID_BOB,
                turnToDecide = ID_ALICE,
                mulliganDecisions = mapOf(
                    ID_ALICE to MulliganDecision.UNDECIDED,
                    ID_BOB to MulliganDecision.MULLIGAN
                )
            )
        )
    }

    val bothPlayersTookFirstMulligan by lazy {
        GameStateFactory.create(
            players = listOf(
                PlayerState(
                    id = ID_ALICE,
                    lifeTotal = 20,
                    // Should have drawn their second hand
                    hand = expectedAliceHand2,
                    // Library should have been shuffled twice now
                    library = DeckFactory.alice.shuffle(2).minus(elements = expectedAliceHand2)
                ),
                PlayerState(
                    id = ID_BOB,
                    lifeTotal = 20,
                    hand = expectedBobHand2,
                    library = DeckFactory.bob.shuffle(2).minus(elements = expectedBobHand2)
                )
            ),
            temporalPosition = ResolvingMulligans(
                numberOfMulligans = 1,
                startingPlayer = ID_BOB,
                turnToDecide = ID_BOB,
                mulliganDecisions = mapOf(
                    // Both go back to undecided, since they have to decide whether to keep new hand
                    ID_ALICE to MulliganDecision.UNDECIDED,
                    ID_BOB to MulliganDecision.UNDECIDED
                )
            )
        )
    }

    val bobDecidedToKeepAfterFirstMulligan by lazy {
        GameStateFactory.create(
            players = listOf(
                // Alice is unchanged
                bothPlayersTookFirstMulligan.players[0],
                PlayerState(
                    id = ID_BOB,
                    lifeTotal = 20,
                    // Bob put the card at index 3 to the bottom
                    hand = expectedBobHand2.minus(expectedBobHand2[3]),
                    library = DeckFactory.bob
                        .shuffle(2)
                        .minus(expectedBobHand2)
                        .plus(expectedBobHand2[3])
                )
            ),
            temporalPosition = ResolvingMulligans(
                numberOfMulligans = 1,
                startingPlayer = ID_BOB,
                turnToDecide = ID_ALICE,
                mulliganDecisions = mapOf(
                    ID_ALICE to MulliganDecision.UNDECIDED,
                    ID_BOB to MulliganDecision.KEEP
                )
            )
        )
    }

    val aliceTookSecondMulligan by lazy {
        GameStateFactory.create(
            players = listOf(
                PlayerState(
                    id = ID_ALICE,
                    lifeTotal = 20,
                    // Alice draws her third hand
                    hand = expectedAliceHand3,
                    library = DeckFactory.alice.shuffle(3).minus(expectedAliceHand3)
                ),
                // Bob is unchanged
                bobDecidedToKeepAfterFirstMulligan.players[1]
            ),
            temporalPosition = ResolvingMulligans(
                numberOfMulligans = 2,
                startingPlayer = ID_BOB,
                turnToDecide = ID_ALICE,
                mulliganDecisions = mapOf(
                    ID_ALICE to MulliganDecision.UNDECIDED,
                    ID_BOB to MulliganDecision.KEEP
                )
            )
        )
    }
    val gameStarted by lazy {
        GameStateFactory.create(
            players = listOf(
                PlayerState(
                    id = ID_ALICE,
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
            temporalPosition = Turn(
                activePlayer = ID_BOB,
                priority = ID_BOB,
                phase = BeginningPhase(
                    step = UpkeepStep
                ),
                firstTurn = true
            )
        )
    }
}
