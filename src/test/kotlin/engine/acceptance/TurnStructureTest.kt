package engine.acceptance

import engine.acceptance.TurnStructureTreeStates.stateWith
import engine.acceptance.TurnStructureTreeStates.turn2Upkeep
import engine.action.DeclareAttackers
import engine.action.PassPriority
import engine.factories.GameStateFactory
import engine.factories.PlayerStateFactory.ID_ALICE
import engine.factories.PlayerStateFactory.ID_BOB
import engine.formats.AllSpellsAreBurnSpells
import engine.reducer.masterReducer
import engine.state.BeginningOfCombatStep
import engine.state.BeginningPhase
import engine.state.CombatPhase
import engine.state.DeclareAttackersStep
import engine.state.DrawStep
import engine.state.EndOfCombatStep
import engine.state.EndStep
import engine.state.EndingPhase
import engine.state.GameState
import engine.state.InvalidPlayerAction
import engine.state.PlayerId
import engine.state.PostCombatMainPhase
import engine.state.PreCombatMainPhase
import engine.state.Turn
import engine.state.TurnPhase
import engine.state.UpkeepStep
import engine.statetree.StateTreeTest
import engine.statetree.TreeMaking.Companion.makeStateTree
import org.junit.jupiter.api.DisplayName

@DisplayName("5. Turn Structure")
class TurnStructureTest : StateTreeTest<GameState>(
    reducer = masterReducer(format = AllSpellsAreBurnSpells()),
    root = root
)

private val root = makeStateTree<GameState> {
    stateWith(phase = BeginningPhase(step = UpkeepStep), priority = ID_ALICE)
        .thenBranch(
            "on upkeep, active player passes priority to non-active player"(
                PassPriority(ID_ALICE) resultsIn stateWith(phase = BeginningPhase(step = UpkeepStep), priority = ID_BOB)
                    .thenChain(
                        "when both players pass priority without doing anything, proceed to draw step"(
                            PassPriority(ID_BOB) resultsIn stateWith(
                                phase = BeginningPhase(step = DrawStep),
                                priority = ID_ALICE
                            )
                        ),
                        "Draw step priority pass"(
                            PassPriority(ID_ALICE) resultsIn stateWith(
                                phase = BeginningPhase(step = DrawStep),
                                priority = ID_BOB
                            )
                        ),
                        "Draw step -> pre-combat main"(
                            PassPriority(ID_BOB) resultsIn stateWith(phase = PreCombatMainPhase, priority = ID_ALICE)
                        ),
                        "Pre-combat main priority pass"(
                            PassPriority(ID_ALICE) resultsIn stateWith(phase = PreCombatMainPhase, priority = ID_BOB)
                        ),
                        "Pre-combat main -> beginning of combat"(
                            PassPriority(ID_BOB) resultsIn stateWith(
                                phase = CombatPhase(step = BeginningOfCombatStep),
                                priority = ID_ALICE
                            )
                        ),
                        "Beginning of combat priority pass"(
                            PassPriority(ID_ALICE) resultsIn stateWith(
                                phase = CombatPhase(step = BeginningOfCombatStep),
                                priority = ID_BOB
                            )
                        ),
                        "Beginning of combat -> declare attackers, neither player has priority"(
                            PassPriority(ID_BOB) resultsIn stateWith(
                                phase = CombatPhase(step = DeclareAttackersStep),
                                priority = null
                            )
                                .thenBranch(
                                    "When active player finishes declaring attackers, they receive priority"(
                                        DeclareAttackers(ID_ALICE) resultsIn stateWith(
                                            phase = CombatPhase(step = DeclareAttackersStep),
                                            priority = ID_ALICE
                                        ).thenChain(
                                            "Declare attackers priority pass"(
                                                PassPriority(ID_ALICE) resultsIn stateWith(
                                                    phase = CombatPhase(step = DeclareAttackersStep),
                                                    priority = ID_BOB
                                                )
                                            ),
                                            "Declare attackers -> End of combat (when no attackers declared)"(
                                                PassPriority(ID_BOB) resultsIn stateWith(
                                                    phase = CombatPhase(step = EndOfCombatStep),
                                                    priority = ID_ALICE
                                                )
                                            ),
                                            "End of combat priority pass"(
                                                PassPriority(ID_ALICE) resultsIn stateWith(
                                                    phase = CombatPhase(step = EndOfCombatStep),
                                                    priority = ID_BOB
                                                )
                                            ),
                                            "End of combat -> Post-combat main phase"(
                                                PassPriority(ID_BOB) resultsIn stateWith(
                                                    phase = PostCombatMainPhase,
                                                    priority = ID_ALICE
                                                )
                                            ),
                                            "Post-combat main phase priority pass"(
                                                PassPriority(ID_ALICE) resultsIn stateWith(
                                                    phase = PostCombatMainPhase,
                                                    priority = ID_BOB
                                                )
                                            ),
                                            "Post-combat main phase -> end step"(
                                                PassPriority(ID_BOB) resultsIn stateWith(
                                                    phase = EndingPhase(step = EndStep),
                                                    priority = ID_ALICE
                                                )
                                            ),
                                            "End step priority pass"(
                                                PassPriority(ID_ALICE) resultsIn stateWith(
                                                    phase = EndingPhase(step = EndStep),
                                                    priority = ID_BOB
                                                )
                                            ),
                                            "After the end step, cleanup occurs and the turn ends. The next player becomes the active player."(
                                                PassPriority(ID_BOB) resultsIn turn2Upkeep
                                            )
                                        )
                                    ),
                                    "Non-active player cannot declare attackers"(
                                        DeclareAttackers(ID_BOB) resultsIn InvalidPlayerAction(
                                            action = DeclareAttackers(ID_BOB),
                                            state = stateWith(
                                                phase = CombatPhase(step = DeclareAttackersStep),
                                                priority = null
                                            ),
                                            reason = "Player $ID_BOB is not the active player"
                                        )
                                    )
                                )
                        )

                    )
            ),
            "players can't pass priority if they don't have it"(
                PassPriority(ID_BOB) resultsIn InvalidPlayerAction(
                    action = PassPriority(ID_BOB),
                    state = stateWith(phase = BeginningPhase(step = UpkeepStep), priority = ID_ALICE),
                    reason = "Player $ID_BOB does not have priority"
                )
            )
        )
}

private object TurnStructureTreeStates {

    fun stateWith(phase: TurnPhase, priority: PlayerId?) = GameStateFactory.create(
        temporalPosition = Turn(
            activePlayer = ID_ALICE,
            phase = phase,
            priority = priority,
            firstTurn = true
        )
    )

    val turn2Upkeep = GameStateFactory.create(
        temporalPosition = Turn(
            activePlayer = ID_BOB,
            phase = BeginningPhase(step = UpkeepStep),
            priority = ID_BOB,
            firstTurn = false
        )
    )
}
