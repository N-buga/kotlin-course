package ru.spbau.mit

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import ru.spbau.mit.parser.LangLexer
import ru.spbau.mit.parser.LangParser
import java.io.File

fun getParser(s: String): LangParser {
    val lexer = LangLexer(CharStreams.fromString(s))
    return LangParser(CommonTokenStream(lexer))
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        throw Exception("No file was passed")
    } else {
        val file = File(args[0])
        EvalVisitor().visit(getParser(file.readText()).file())
    }
}

