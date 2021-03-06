package engine.state

import kotlinx.serialization.Serializable

@Serializable
data class RandomRequest(
    val shuffles: List<Int> = emptyList(),
    val randomNumbers: List<Range> = emptyList()
)

@Serializable
data class Range(val first: Int, val last: Int) {
    constructor(intRange: IntRange) : this(intRange.first, intRange.last)
}
