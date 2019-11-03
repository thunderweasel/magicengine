package engine.shuffler

/**
 * Shuffler that doesn't randomize anything, just moves a selected list
 * of cards to the top, in the order specified. The rest of the deck order
 * is unaltered.
 *
 * Cards that are not actually in the deck will just be ignored.
 */
class CheatShuffleToTop<T>(
    private val cardsToTop: List<T>
) : Shuffler<T> {
    override fun shuffle(cards: List<T>) =
        cards.toMutableList().apply {
            for (card in cardsToTop.reversed()) {
                val index = indexOf(card)
                if (index != -1) {
                    removeAt(index)
                    add(0, card)
                }
            }
        }
}