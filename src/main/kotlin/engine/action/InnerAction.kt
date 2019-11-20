package engine.action

import kotlinx.serialization.Serializable

@Serializable
sealed class InnerAction
@Serializable
object ElectDeciderOfStartingPlayer : InnerAction()
@Serializable
object PerformMulligans : InnerAction()
