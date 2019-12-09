package engine.state

typealias Battlefield = Map<PermanentId, Permanent>

fun createBattlefield(vararg permanents: Permanent): Battlefield = permanents
    .map(::toBattlefieldEntry)
    .toMap()

fun Battlefield.adding(vararg permanents: Permanent): Battlefield =
    plus(permanents.map(::toBattlefieldEntry))

inline fun Battlefield.replacing(replaceId: PermanentId, crossinline compute: Permanent.() -> Permanent): Battlefield =
    mapValues { (id, permanent) ->
        if (id == replaceId) {
            compute(permanent)
        } else {
            permanent
        }
    }

private fun toBattlefieldEntry(permanent: Permanent) = permanent.id to permanent
