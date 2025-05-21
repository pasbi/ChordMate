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

fun rootName(root: Int): String = arrayOf("C", "C#", "D", "Eb", "E", "F", "Fis", "G", "Ab", "A", "Bb", "B")[root.mod(12)]

data class Chord(
    val root: Int,
    val suffix: String,
    val upperCase: Boolean,
) {
    override fun toString(): String {
        var root = rootName(root)
        if (!upperCase) {
            root = root.lowercase()
        }
        return root + suffix
    }
}

fun parseChord(text: String): Chord? {
    val options = RegexOption.IGNORE_CASE
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
            val strippedText = text.replaceRange(match.range, "")
            if (Regex("""(m|5|6|7|7\+|°|sus|sus2|sus4|add2|)*""", options).matches(strippedText)) {
                return Chord(root.mod(12), strippedText, match.groupValues[1].first().isUpperCase())
            }
        }
    }
    return null
}

class Token(
    val text: String,
    val position: Int,
) {
    val chord: Chord? = parseChord(text)

    fun isChord() = chord != null

    fun text(): String {
        if (chord != null) {
            return chord.toString()
        }
        return text
    }

    override fun toString(): String {
        val isChord = if (isChord()) "c" else "nc"
        return "Token[$text, @$position, $isChord]"
    }
}

fun isChordLine(tokens: List<Token>): Boolean {
    val ambiguousWords = setOf("a", "as", "ab", "es")
    if (tokens.find { it.isChord() && !ambiguousWords.contains(it.text) } != null) {
        // There is a token which is a chord and not a word.
        return true
    }
    if (tokens.count { it.isChord() } * 2 > tokens.size) {
        // More than half the tokens are valid chords.
        return true
    }
    return false
}

fun tokenize(line: String): List<Token> = Regex("""\b\S+\b""").findAll(line).map { Token(it.value, it.range.start) }.toList()

private fun merge(
    chords: List<Token>,
    textLine: String,
): String {
    for ((a, b) in chords.zipWithNext()) {
        assert(a.position < b.position)
    }
    var sb = StringBuilder()
    sb.append(textLine)
    sb.append(" ".repeat(max(0, chords.last().position - textLine.length)))
    var accu = 0
    for (chord in chords.reversed()) {
        println("Insert $chord into '$sb'")
        sb.insert(chord.position, "[${chord.text}]")
        accu += 1 // chord.text.length
    }
    return sb.toString() // .trimEnd()
}

class TokenizedLine(
    val tokens: List<Token>,
    val text: String,
)

private fun merge(
    line: TokenizedLine,
    nextLine: TokenizedLine,
): Pair<String, Boolean> =
    if (isChordLine(line.tokens)) {
        if (isChordLine(nextLine.tokens)) {
            merge(line.tokens, "") to false // skip next
        } else {
            merge(line.tokens, nextLine.text) to true
        }
    } else {
        line.text to false
    }

fun toChordPro(code: String?): String {
    if (code == null) {
        return ""
    }
    val lines = code!!.split("\n").map { s -> TokenizedLine(tokenize(s), s) }
    var skip = false
    var resultLines: MutableList<String> = ArrayList<String>()
    for ((line, nextLine) in lines.zipWithNext()) {
        if (skip) {
            skip = false
            continue
        }

        val (result, skipNext) = merge(line, nextLine)
        skip = skipNext
        resultLines.add(result)
    }
    if (!skip && lines.isNotEmpty()) {
        val (lastLine, _) = merge(lines.last(), TokenizedLine(ArrayList<Token>(), ""))
        resultLines.add(lastLine)
    }
    return resultLines.joinToString("\n")
}
