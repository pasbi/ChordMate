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
