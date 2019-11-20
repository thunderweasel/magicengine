package engine.action

sealed class InnerAction
object RandomizeChoiceForFirst : InnerAction()
object PerformMulligans : InnerAction()
