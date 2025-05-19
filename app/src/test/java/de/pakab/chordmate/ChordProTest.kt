package de.pakab.chordmate

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ChordProTest(
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
    fun `test cp`() {
        Assert.assertEquals(expectedRender, renderChordPro(pattern))
    }
}
