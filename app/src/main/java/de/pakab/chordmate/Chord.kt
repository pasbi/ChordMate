package de.pakab.chordmate

fun parseChord(
    line: String,
    range: IntRange,
): Chord? {
    val options = RegexOption.IGNORE_CASE
    val map =
        mapOf<Regex, Int>( // order matters
            Regex("""(^[CDEFGAB])(is|#|♯)""", options) to 1,
            Regex("""(^[CDFGB])(es|b|♭)""", options) to -1,
            Regex("""(^[AE])([sb♭])""", options) to -1,
            Regex("""(^[CDEFGAB])""", options) to 0,
        )
    for ((regex, pitch) in map) {
        println("line='$line', range=$range")
        val text = line.substring(range)
        val match = regex.find(text)
        if (match != null) {
            val root = rootValue(match.groupValues[1].uppercase())!! + pitch
            val strippedText = text.replaceRange(match.range, "")
            if (Regex("""(m|5|6|7|7\+|°|sus|sus2|sus4|add2|)*""", options).matches(strippedText)) {
                return Chord(root.mod(12), strippedText, match.value.first().isUpperCase(), range)
            }
        }
    }
    return null
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
    val location: IntRange? = null,
) {
    override fun toString(): String {
        var root = rootName(root)
        if (!upperCase) {
            root = root.lowercase()
        }
        return root + suffix
    }

    fun transposed(semitones: Int): Chord = Chord(root + semitones, suffix, upperCase, null)
}

fun findLargestSubString(
    s: String,
    start: Int,
): Chord? {
    var largest: Chord? = null
    for (i in start + 0..<s.length) {
        parseChord(s, IntRange(start, i))?.let { largest = it }
    }
    return largest
}

fun len(range: IntRange): Int = range.last - range.start + 1

fun findChords(line: String): List<Chord> {
    val chords: MutableList<Chord> = ArrayList<Chord>()
    var start = 0
    while (start < line.length) {
        val chord = findLargestSubString(line, start)
        if (chord != null) {
            chords.add(chord)
            start += len(chord.location!!)
        } else {
            start += 1
        }
    }
    return chords
}

fun transposeLine(
    line: String,
    semitones: Int,
): String {
    val chords = findChords(line)
    var newLine = line
    for (chord in chords.reversed()) {
        var newChord = chord.transposed(semitones).toString()
        val oldChordLen = len(chord.location!!)
        (
            oldChordLen -
                newChord.length
        ).apply {
            if (this > 0) {
                newChord = "$newChord${" ".repeat(this)}"
            }
        }
        newLine = newLine.replaceRange(chord.location!!, newChord)
    }
    return newLine
}

fun transpose(
    lines: String,
    semitones: Int,
): String = lines.split("\n").joinToString("\n") { transposeLine(it, semitones) }.trim()

fun isChordLine(line: String): Boolean {
    val chords = findChords(line)
    var replaced = line
    for (chord in chords.reversed()) {
        replaced = replaced.replaceRange(chord.location!!, " ")
    }
    val nonChords = replaced.split(Regex("""\s+"""))
    return chords.size > nonChords.size
}
