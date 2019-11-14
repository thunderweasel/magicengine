package engine.model

typealias CardId = Long

fun Int.toCardId(): CardId = toLong()