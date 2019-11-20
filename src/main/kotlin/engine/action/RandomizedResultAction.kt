package engine.action

data class RandomizedResultAction(
    val innerAction: InnerAction,
    val resolvedRandomization: ResolvedRandomization
) : GameAction
