package engine.action

data class RandomizedResultAction(
    val innerAction: InnerAction,
    val resolvedRandomization: ResolvedRandomization
) : GameAction {
    sealed class InnerAction {
        object RandomizeChoiceForFirst : InnerAction()
        object PerformMulligans : InnerAction()
    }
}
