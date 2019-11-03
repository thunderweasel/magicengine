package engine.shuffler

class FakeRandomizer(
    private val randomValues: List<Int>
) : Randomizer {
    private var next = 0

    override fun randomInt(from: Int, to: Int) =
        if (next < randomValues.size && randomValues[next] in from..to) {
            randomValues[next++]
        } else {
            throw IllegalStateException("Next random value not in expected range!")
        }
}