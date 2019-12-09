package engine.state

import engine.domain.ManaType

typealias ManaPool = Map<ManaType, Long>

fun createManaPool(
    vararg entries: Pair<ManaType, Long>
): ManaPool = ManaType.all
    .map { manaType ->
        manaType to sumManaOfType(entries, manaType)
    }
    .toMap()

private fun sumManaOfType(
    entries: Array<out Pair<ManaType, Long>>,
    manaType: ManaType
): Long {
    val filtered = entries.filter { it.first == manaType }
    return if (filtered.isNotEmpty()) {
        filtered
            .map { it.second }
            .reduce(Long::plus)
    } else {
        0
    }
}

fun ManaPool.adding(vararg additions: Pair<ManaType, Long>): ManaPool {
    return createManaPool(*entries.map { it.key to it.value }.plus(additions).toTypedArray())
}
