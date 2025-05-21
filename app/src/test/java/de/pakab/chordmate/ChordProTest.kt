package de.pakab.chordmate

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class RenderChordProTest(
    private val pattern: String,
    private val expectedRender: String,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters()
        fun get(): Iterable<Array<Any>> =
            arrayListOf(
                arrayOf("Foo", "Foo"),
                arrayOf("[C]Foo", "C\nFoo"),
                arrayOf(
                    "[C]abc [Dm7+]x [A]y",
                    "C   Dm7+ A\n" +
                        "abc x y",
                ),
            )
    }

    @Test
    fun renderChordProTest() {
        Assert.assertEquals(expectedRender, renderChordPro(pattern))
    }
}

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
        println(tokenize(line))
        Assert.assertEquals(expectedChordLine, isChordLine(tokenize(line)))
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
        Assert.assertEquals(chord, parseChord(pattern))
    }
}

@RunWith(Parameterized::class)
class ToChordPro(
    private val text: String,
    private val chordPro: String,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters()
        fun get(): Iterable<Array<Any?>> =
            arrayListOf(
                arrayOf("C\nX", "[C]X"),
                arrayOf("C dm7+sus4add2\nX Y", "[C]X [dm7+sus4add2]Y"),
                arrayOf("C d\nX", "[C]X [d]"),
                arrayOf("C d     A\nX", "[C]X [d]      [A]"),
                arrayOf(" C d\nX", "X[C]  [d]"),
                arrayOf(" C d\nX ABC DEF", "X[C] A[d]BC DEF"),
                arrayOf("  C\n", "  [C]"),
                arrayOf("  C", "  [C]"),
                arrayOf("  C\n  D\n  E", "  [C]\n  [D]\n  [E]"),
                arrayOf("foo bar\n  C\n  D\n  E", "foo bar\n  [C]\n  [D]\n  [E]"),
                arrayOf("foo bar\n  C\n  D\n  E\ncaffe", "foo bar\n  [C]\n  [D]\nca[E]ffe"),
                arrayOf("foo bar", "foo bar"),
                arrayOf("foo\nbar", "foo\nbar"),
                arrayOf("", ""),
                arrayOf("\n", "\n"),
            )
    }

    @Test
    fun toChordProTest() {
        Assert.assertEquals(chordPro, toChordPro(text))
    }
}
