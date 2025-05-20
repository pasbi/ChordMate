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
            )
    }

    @Test
    fun renderChordProTest() {
        Assert.assertEquals(chord, parseChord(pattern))
    }
}
