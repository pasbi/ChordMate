package de.pakab.chordmate

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DetectChordLineTest(
    private val line: String,
    private val expectedChordLine: Boolean,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters()
        fun get(): Iterable<Array<Any?>> =
            arrayListOf(
                arrayOf("C", true),
                arrayOf("C C C", true),
                arrayOf("as of as of", false),
                arrayOf("as C C", true),
                arrayOf("foo bar baz", false),
                arrayOf("foo bar baz C C C C", true),
            )
    }

    @Test
    fun detectChordLineTest() {
        Assert.assertEquals(expectedChordLine, isChordLine(line))
    }
}

@RunWith(Parameterized::class)
class ParseChordTest(
    private val pattern: String,
    private val chord: Chord?,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters()
        fun get(): Iterable<Array<Any?>> =
            arrayListOf(
                arrayOf("C", Chord(0, "", true)),
                arrayOf("F", Chord(5, "", true)),
                arrayOf("Fis", Chord(6, "", true)),
                arrayOf("Ges", Chord(6, "", true)),
                arrayOf("Ces", Chord(11, "", true)),
                arrayOf("Bis", Chord(0, "", true)),
                arrayOf("c", Chord(0, "", false)),
                arrayOf("cm", Chord(0, "m", false)),
                arrayOf("Cm", Chord(0, "m", true)),
                arrayOf("C#m7+Sus4", Chord(1, "m7+Sus4", true)),
                arrayOf("X", null),
                arrayOf("foo", null),
                arrayOf("bar", null),
            )
    }

    @Test
    fun parseChordTest() {
        Assert.assertEquals(chord.toString(), parseChord(pattern, 0..<pattern.length).toString())
    }
}

@RunWith(Parameterized::class)
class TransposeChordTest(
    private val oldLine: String,
    private val transposing: Int,
    private val newLine: String,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters()
        fun get(): Iterable<Array<Any?>> =
            arrayListOf(
                arrayOf("C", 1, "C#"),
                arrayOf("C C", 1, "C# C#"),
                arrayOf("C# C#", -1, "C  C"),
            )
    }

    @Test
    fun parseChordTest() {
        Assert.assertEquals(newLine, transpose(oldLine, transposing))
    }
}
