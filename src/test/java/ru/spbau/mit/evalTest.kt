package ru.spbau.mit

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.spbau.mit.parser.LangLexer
import ru.spbau.mit.parser.LangParser
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class LangEvalTest(private val programPath: Path, private val answer: String) {
    private val outContent = ByteArrayOutputStream()

    private fun getParser(s: String): LangParser {
        val lexer = LangLexer(CharStreams.fromString(s))
        return LangParser(CommonTokenStream(lexer))
    }

    companion object {
        private val testDirectory = Paths.get("src", "test", "java", "ru", "spbau", "mit").toString()

        @JvmStatic
        @Parameterized.Parameters
        fun data(): List<Array<Comparable<*>>> {
            return listOf(
                    arrayOf(Paths.get(testDirectory, "test1.lang"), "42\n"),
                    arrayOf(Paths.get(testDirectory, "test2.lang"), "0\n"),
                    arrayOf(Paths.get(testDirectory, "test3.lang"), "1 1\n2 2\n3 3\n4 5\n5 8\n")
            )
        }
    }

    @Before
    fun setUpStream() {
        System.setOut(PrintStream(outContent));
    }

    @After
    fun cleanUpStream() {
        System.setOut(null);
    }

    @Test
    fun evalTest() {
        EvalVisitor().visit(getParser(programPath.toFile().readText()).file())

        assertEquals(answer, outContent.toString())
    }
}
