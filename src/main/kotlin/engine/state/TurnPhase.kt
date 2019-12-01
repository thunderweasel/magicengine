package engine.state

import kotlinx.serialization.Serializable

@Serializable
sealed class TurnPhase
@Serializable
data class BeginningPhase(
    val step: BeginningPhaseStep
) : TurnPhase()
@Serializable
object PreCombatMainPhase : TurnPhase()
@Serializable
data class CombatPhase(
    val step: CombatStep
) : TurnPhase()
@Serializable
object PostCombatMainPhase : TurnPhase()
@Serializable
data class EndingPhase(
    val step: EndingStep
) : TurnPhase()

@Serializable
sealed class BeginningPhaseStep
@Serializable
object UntapStep : BeginningPhaseStep()
@Serializable
object UpkeepStep : BeginningPhaseStep()
@Serializable
object DrawStep : BeginningPhaseStep()

@Serializable
sealed class CombatStep
@Serializable
object BeginningOfCombatStep : CombatStep()
@Serializable
object DeclareAttackersStep : CombatStep()
@Serializable
object DeclareBlockersStep : CombatStep()
@Serializable
object CombatDamageStep : CombatStep()
@Serializable
object EndOfCombatStep : CombatStep()

@Serializable
sealed class EndingStep
@Serializable
object EndStep : EndingStep()
@Serializable
object CleanupStep : EndingStep()
