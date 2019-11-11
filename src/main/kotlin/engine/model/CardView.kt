package engine.model

sealed class CardView {
    data class KnownCard(val card: Card): CardView(), Card by card {
        override fun toString() = name
    }
    object UnknownCard: CardView() {
        override fun toString() = "UNKNOWN"
    }
}