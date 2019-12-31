package engine.cards

import engine.domain.Cost
import engine.domain.ManaType
import engine.reducer.replacePlayerState
import engine.state.ActivatedAbility
import engine.state.GameState
import engine.state.GameStatePendingRandomization
import engine.state.adding
import engine.state.noPendingRandomization

// TODO: Should this belong in the core module or somewhere else?
data class BasicLandSpec(
    override val name: String,
    override val subtypes: List<String> = listOf(name),
    val manaType: ManaType
) : CardSpec {
    val manaAbility by lazy {
        object : ActivatedAbilitySpec {
            override val id: AbilitySpecId
                get() = AbilitySpecId(
                    name,
                    1
                )
            override val isManaAbility: Boolean get() = true
            override val resolve: (GameState, ActivatedAbility) -> GameStatePendingRandomization
                get() = { state, ability ->
                    val permanent = state.battlefield[ability.permanentId]
                    require(permanent != null)
                    state.copy(
                        players = state.replacePlayerState(permanent.controller) {
                            copy(
                                manaPool = manaPool.adding(manaType to 1L)
                            )
                        }
                    ).noPendingRandomization()
                }
            override val costs = listOf(Cost.Tap)
        }
    }
    override val isBasicLand get() = true
    override val activatedAbilities: List<ActivatedAbilitySpec> = listOf(manaAbility)
    override val cardTypes = listOf(CardType.LAND)
}

val PlainsSpec = BasicLandSpec(
    name = "Plains",
    manaType = ManaType.WHITE
)

val IslandSpec = BasicLandSpec(
    name = "Island",
    manaType = ManaType.BLUE
)

val SwampSpec = BasicLandSpec(
    name = "Swamp",
    manaType = ManaType.BLACK
)

val MountainSpec = BasicLandSpec(
    name = "Mountain",
    manaType = ManaType.RED
)

val ForestSpec = BasicLandSpec(
    name = "Forest",
    manaType = ManaType.GREEN
)

val allBasicLands = listOf(
    PlainsSpec,
    IslandSpec,
    SwampSpec,
    MountainSpec,
    ForestSpec
)
