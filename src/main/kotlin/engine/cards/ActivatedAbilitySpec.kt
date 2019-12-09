package engine.cards

import engine.domain.Cost
import engine.state.ActivatedAbility
import engine.state.GameState
import engine.state.GameStatePendingRandomization

interface ActivatedAbilitySpec {
    val id: AbilitySpecId
    val isManaAbility: Boolean
    val costs: List<Cost>
    val resolve: (GameState, ActivatedAbility) -> GameStatePendingRandomization
}
