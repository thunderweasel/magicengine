package engine.cards

interface CardSpecLookup {
    /**
     * Get a [CardSpec] by name.
     * @param name the name of the card, which should always uniquely identify a [CardSpec]
     * @return a [CardSpec] with a name matching [name], or null if there are no cards with that name
     */
    operator fun get(name: String): CardSpec?
}
