package de.pakab.chordmate

import kotlin.math.max

class Chord(
    val position: Int,
    val text: StringBuilder,
)

private fun renderChordProLine(line: String): Array<String> {
    val regex = Regex("""\[.*?\]""")
    val matchResults = regex.findAll(line).toList()
    if (matchResults.isEmpty()) {
        return arrayOf(line)
    }

    var strippedLine = line
    for (mr in matchResults.asReversed()) {
        strippedLine = strippedLine.replaceRange(mr.range, "")
    }
    var positions: MutableList<Int> = ArrayList<Int>()
    var accumulator = 0
    for (mr in matchResults) {
        positions.add(mr.range.start - accumulator)
        accumulator += mr.range.last - mr.range.start + 1
    }

    val sb = StringBuilder()
    for ((mr, pos) in (matchResults zip positions)) {
        val symbol = mr.value.substring(1, mr.value.length - 1)
        val minSpaceBeforeSymbol = if (pos == 0) 0 else 1
        val n = max(minSpaceBeforeSymbol, pos - sb.length)
        sb.append(" ".repeat(n))
        sb.append(symbol)
    }
    return arrayOf(sb.toString(), strippedLine)
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
