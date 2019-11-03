package engine.model

import engine.domain.PlayerId

data class PlayerState(
    val id: PlayerId,
    val hand: List<Card> = listOf(),
    val library: List<Card> = listOf(),
    val lifeTotal: Long,
    val mulliganDecision: MulliganDecision
)