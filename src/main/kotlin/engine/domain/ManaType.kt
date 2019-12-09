package engine.domain

enum class ManaType {
    WHITE,
    BLUE,
    BLACK,
    RED,
    GREEN,
    COLORLESS;

    companion object {
        val all = sequenceOf(
            WHITE,
            BLUE,
            BLACK,
            RED,
            GREEN,
            COLORLESS
        )
    }
}
