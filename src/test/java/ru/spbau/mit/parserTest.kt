package ru.spbau.mit
import org.antlr.v4.runtime.CharStreams
import ru.spbau.mit.parser.LangLexer
import ru.spbau.mit.parser.LangParser
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File
import java.nio.file.Paths
import kotlin.test.assertEquals
import org.junit.Test as test

class LangParserTest {
    fun getParser(s: String): LangParser {
        val lexer = LangLexer(CharStreams.fromString(s))
        return LangParser(CommonTokenStream(lexer))
    }

    @test
    fun parseCode1() {
        val testFile = Paths.get("src", "test", "java", "ru", "spbau", "mit", "test1.lang").toFile()
        val parser = getParser(testFile.readText())

        assertEquals(toParseTree(parser.file()).multiLineString(),
"""File
  Body
    Statement
      FunctionDeclaration
        T[fun]
        T[foo]
        T[(]
        ParameterNames
          T[n]
        T[)]
        BodyWithBraces
          T[{]
          Body
            Statement
              FunctionDeclaration
                T[fun]
                T[bar]
                T[(]
                ParameterNames
                  T[m]
                T[)]
                BodyWithBraces
                  T[{]
                  Body
                    Statement
                      ReturnExpression
                        T[return]
                        AdditiveExpr
                          OperationUnitExpr
                            UnitID
                              T[m]
                          T[+]
                          OperationUnitExpr
                            UnitID
                              T[n]
                  T[}]
            Statement
              ReturnExpression
                T[return]
                FunctionCallExpr
                  FunctionCall
                    T[bar]
                    T[(]
                    Arguments
                      OperationUnitExpr
                        UnitLiteral
                          T[1]
                    T[)]
          T[}]
    Statement
      FunctionCallExpr
        FunctionCall
          T[println]
          T[(]
          Arguments
            FunctionCallExpr
              FunctionCall
                T[foo]
                T[(]
                Arguments
                  OperationUnitExpr
                    UnitLiteral
                      T[41]
                T[)]
          T[)]
  T[<EOF>]
"""
        )
    }


}
