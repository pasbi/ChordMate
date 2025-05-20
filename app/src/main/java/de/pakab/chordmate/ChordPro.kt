package de.pakab.chordmate

import kotlin.math.max

private fun findChordPositions(matchResults: List<MatchResult>): List<Int> {
    var positions: MutableList<Int> = ArrayList<Int>()
    var accumulator = 0
    for (mr in matchResults) {
        positions.add(mr.range.start - accumulator)
        accumulator += mr.range.last - mr.range.start + 1
    }
    return positions
}

private fun stripChords(
    line: String,
    matchResults: List<MatchResult>,
): String {
    var strippedLine = line
    for (mr in matchResults.asReversed()) {
        strippedLine = strippedLine.replaceRange(mr.range, "")
    }
    return strippedLine
}

private fun assembleChordLine(
    matchResults: List<MatchResult>,
    chordPositions: List<Int>,
): String {
    val sb = StringBuilder()
    for ((mr, pos) in (matchResults zip chordPositions)) {
        val symbol = mr.value.substring(1, mr.value.length - 1)
        val minSpaceBeforeSymbol = if (pos == 0) 0 else 1
        val n = max(minSpaceBeforeSymbol, pos - sb.length)
        sb.append(" ".repeat(n))
        sb.append(symbol)
    }
    return sb.toString()
}

private fun renderChordProLine(line: String): Array<String> {
    val regex = Regex("""\[.*?\]""")
    val matchResults = regex.findAll(line).toList()
    if (matchResults.isEmpty()) {
        return arrayOf(line)
    }

    val chordLine = assembleChordLine(matchResults, findChordPositions(matchResults))
    val textLine = stripChords(line, matchResults)
    return arrayOf(chordLine, textLine)
}

fun renderChordPro(code: String?): String {
    if (code == null) {
        return ""
    }
    var lines: MutableList<String> = ArrayList<String>()
    for (line in code!!.split("\n")) {
        for (renderedLine in renderChordProLine(line)) {
            lines.add(renderedLine)
        }
    }
    return lines.joinToString("\n")
}

fun rootValue(text: String): Int? =
    when (text) {
        "C" -> 0
        "D" -> 2
        "E" -> 4
        "F" -> 5
        "G" -> 7
        "A" -> 9
        "B" -> 11
        else -> null
    }

data class Chord(
    val root: Int,
    val suffix: String,
    val upperCase: Boolean,
)

fun parseChord(text: String): Chord? {
    println("====\n$text")
    val options = setOf(RegexOption.IGNORE_CASE)
    val map =
        mapOf<Regex, Int>( // order matters
            Regex("""(^[CDEFGAB])(is|#|♯)""", options) to 1,
            Regex("""(^[CDFGB])(es|b|♭)""", options) to -1,
            Regex("""(^[AE])(s|b|♭)""", options) to -1,
            Regex("""(^[CDEFGAB])""", options) to 0,
        )
    for ((regex, pitch) in map) {
        val match = regex.find(text)
        if (match != null) {
            val root = rootValue(match.groupValues[1].uppercase())!! + pitch
            println("  $root  ${match.groupValues[1].first()}")
            val strippedText = text.replaceRange(match.range, "")
            println("text = $text, range=${match.range}")
            return Chord(root.mod(12), strippedText, match.groupValues[1].first().isUpperCase())
        }
    }
    return null
}

class Token(
    val text: String,
    val position: Int,
) {
    val chord: Chord? = parseChord(text)

    fun valid() = chord != null

    fun text(): String = "F"
}

private fun isChordLine(chords: List<Token>): Boolean = false

private fun merge(
    chords: List<Token>,
    textLine: String,
): String {
    if (chords.isEmpty()) {
        return textLine
    }

    var tokens: MutableList<String> = ArrayList<String>()
    tokens.add(textLine.substring(0, chords.first().position))
    for ((chord, nextChord) in chords.zipWithNext()) {
        tokens.add(textLine.substring(chord.position, nextChord.position))
    }
    tokens.add(textLine.substring(chords.last().position, textLine.length))

    val sb = StringBuilder()
    for ((token, chord) in (tokens zip chords)) {
        sb.append(token)
        sb.append("[${chord.text()}]")
    }
    sb.append(tokens.last())
    return sb.toString()
}

fun toChordPro(code: String?): String {
    if (code == null) {
        return ""
    }
    var lines: MutableList<String> = ArrayList<String>()
    var skip = false
    for ((line, nextLine) in code!!.split("\n").zipWithNext()) {
        if (skip) {
            skip = false
            continue
        }
        val chords = Regex("""(\S+)""").findAll(line).map { matchResult -> Token(matchResult.value, matchResult.range.first) }.toList()

        if (isChordLine(chords)) {
            skip = true
            lines.add(merge(chords, nextLine))
        } else {
            lines.add(line)
        }
    }
    return ""
}
