package ru.spbau.mit

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.spbau.mit.parser.LangLexer
import ru.spbau.mit.parser.LangParser
import org.junit.Test as test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals


@RunWith(Parameterized::class)
class LangSimpleEvalTest(private val code: String, private val answer: String) {
    private val outContent = ByteArrayOutputStream()

    private fun getParser(s: String): LangParser {
        val lexer = LangLexer(CharStreams.fromString(s))
        return LangParser(CommonTokenStream(lexer))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() : Collection<Array<String>> {
            return listOf(
                    arrayOf("println(1)", "1\n"),
                    arrayOf("println(1, 2)", "1 2\n"),
                    arrayOf("println(1, 2, 3)", "1 2 3\n"),
                    arrayOf("println(1+2)", "3\n"),
                    arrayOf("println((1+3))", "4\n"),
                    arrayOf("println(2*2+3)", "7\n"),
                    arrayOf("println(5/3)", "1\n"),
                    arrayOf("println(9 - 8+4)", "5\n"),
                    arrayOf("println(9 - 8*5)", "-31\n"),
                    arrayOf("println(5 == 5)", "1\n"),
                    arrayOf("println(5 == 4)","0\n"),
                    arrayOf("println(5 && 4)", "1\n"),
                    arrayOf("println(0 && 1)", "0\n"),
                    arrayOf("println(6 || 0)", "1\n"),
                    arrayOf("fun go(x) { println(x) } go(4)", "4\n"),
                    arrayOf("var x = 4 var y = 5 println(x + y)", "9\n"),
                    arrayOf("var x x = 5 println((x + 1)*2)", "12\n"),
                    arrayOf("var ama = 4 println(ama) var x = 2 println(ama + x, 3)", "4\n6 3\n"),
                    arrayOf("fun go(x, y) { println(x + y) } var m = 4 go(m, 5)", "9\n"),
                    arrayOf("fun go(x, y) { return x + y } var z = go(1, 1) println(z)", "2\n"),
                    arrayOf("var x = 5 fun add(x) { return x + 1} add(3) println(x)", "5\n"),
                    arrayOf("if (1 == 1) {println(1)}", "1\n"),
                    arrayOf("if (1 == 1) {var x = 1}", ""),
                    arrayOf("if (1 == 0) {println(1)}", ""),
                    arrayOf("var x = 1 if (x == 1) {println(x + 1)} else {println(x + 2)}", "2\n"),
                    arrayOf("var x = 2 if (x == 1) {println(x + 1)} else {println(x + 2)}", "4\n"),
                    arrayOf("var x = 1 while (x < 3) {println(x) x = x + 1}", "1\n2\n"),
                    arrayOf("fun pr() { println(1) } pr()", "1\n"),
                    arrayOf("var x = 4 fun pr() { var x = 3 println(x) } pr() println(x)", "3\n4\n")
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

    @test
    fun evalTest() {
        EvalVisitor().visit(getParser(code).file())

        assertEquals(answer, outContent.toString())
    }
}