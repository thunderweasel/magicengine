package engine.action

sealed class InnerAction
object ElectDeciderOfStartingPlayer : InnerAction()
object PerformMulligans : InnerAction()
