package com.rebootech.fruitlogix.shared.ui.format

private val WHITESPACE = Regex("\\s+")

/** "Alexandra Jiménez" -> "AJ", "Mercado Central S.A." -> "MC". */
fun initialsOf(name: String): String =
    name.trim().split(WHITESPACE).filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }
