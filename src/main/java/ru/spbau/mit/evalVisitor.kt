package ru.spbau.mit

import ru.spbau.mit.parser.LangParser
import ru.spbau.mit.parser.LangParser.*
import ru.spbau.mit.parser.LangParserBaseVisitor
import kotlin.collections.HashMap


class EvalVisitor: LangParserBaseVisitor<Int>() {

    private val buildIn: Map<String, (List<Int>) -> Int?> = hashMapOf(
            ("println" to {list ->
                println(list.map{it -> it.toString()}.joinToString(" "))
                1
            })
    )
    private val stackScopes: MutableList<Scope> = mutableListOf(Scope())
    private var returnVal: Int? = null

    override fun visitBodyWithBraces(ctx: LangParser.BodyWithBracesContext): Int? {
        stackScopes.add(Scope())
        val ans = ctx.body().accept(this)
        stackScopes.remove(stackScopes.last())
        return ans
    }

    override fun visitBody(ctx: BodyContext): Int? {
        var result: Int? = null
        for (child in ctx.children) {
            result = child.accept(this)
            if (returnVal != null) {
                return returnVal
            }
        }
        return result
    }

    override fun visitReturnExpression(ctx: ReturnExpressionContext): Int? {
        returnVal = ctx.expression().accept(this)
        return returnVal
    }

    override fun visitParanthesisExpr(ctx: ParanthesisExprContext): Int {
        return ctx.expression().accept(this)
    }

    override fun visitFunctionCall(ctx: FunctionCallContext): Int? {
        val args: List<Int> = ctx.arguments().expression().map { expr -> expr.accept(this) }
        val funcName = ctx.IDENTIFIER().text

        if (funcName in buildIn) {
            return buildIn[funcName]?.invoke(args)
        } else {
            val scope = stackScopes.findLast { scope -> scope.haveFunc(funcName) }
            if (scope == null) {
                throw Exception(formatErrOut(
                        ctx.IDENTIFIER().symbol.line,
                        ctx.IDENTIFIER().symbol.charPositionInLine,
                        "Can't find declaration of func $funcName"
                ))
            } else {
                val funcCtx = scope.getFunc(funcName)
                if (funcCtx != null) {
                    val variables = funcCtx.parameterNames().IDENTIFIER().map { id -> id.text }
                    if (variables.size != args.size) {
                        throw Exception(formatErrOut(
                                ctx.IDENTIFIER().symbol.line,
                                ctx.IDENTIFIER().symbol.charPositionInLine,
                                "Wrong count of arguments in function $funcName"
                        ))
                    } else {
                        stackScopes.add(Scope())
                        (0..(variables.size - 1)).forEach { num -> stackScopes.last().addVar(variables[num], args[num]) }
                        val returnValue = funcCtx.bodyWithBraces().accept(this)
                        returnVal = null
                        stackScopes.remove(stackScopes.last())
                        return returnValue
                    }
                } else {
                    throw Exception(formatErrOut(
                            ctx.IDENTIFIER().symbol.line,
                            ctx.IDENTIFIER().symbol.charPositionInLine,
                            "Can't find declaration of func $funcName"
                    ))
                }
            }
        }
    }

    override fun visitFunctionDeclaration(ctx: LangParser.FunctionDeclarationContext): Int? {
        val funcName = ctx.IDENTIFIER().text
        // check if exists in scope
        if (stackScopes.any { scope -> scope.haveFunc(funcName) } || buildIn.keys.any{ key -> key == funcName})
            throw Exception(formatErrOut(
                    ctx.IDENTIFIER().symbol.line,
                    ctx.IDENTIFIER().symbol.charPositionInLine,
                    "Function name duplicate $funcName"
            ))

        // add name and body to scope
        val currentScope = stackScopes.last()
        currentScope.addFunc(funcName, ctx)

        return 1
    }

    override fun visitVariableDeclaration(ctx: VariableDeclarationContext): Int {
        val varName = ctx.IDENTIFIER().text

        if (stackScopes.any { scope -> scope.haveVar(varName) })
            throw Exception(formatErrOut(
                    ctx.IDENTIFIER().symbol.line,
                    ctx.IDENTIFIER().symbol.charPositionInLine,
                    "Function name duplicate $varName"
            ))

        val currentScope = stackScopes.last()
        currentScope.addVar(varName)

        val value = ctx.expression()?.accept(this)
        if (value != null) {
            currentScope.assignVar(varName, value)
        }
        return 1
    }

    override fun visitAssigment(ctx: AssigmentContext): Int {
        val varName = ctx.IDENTIFIER().text

        if (stackScopes.none { scope -> scope.haveVar(varName) })
            throw Exception(formatErrOut(
                    ctx.IDENTIFIER().symbol.line,
                    ctx.IDENTIFIER().symbol.charPositionInLine,
                    "Can't find variable $varName"
            ))

        val scope = stackScopes.findLast { scope -> scope.haveVar(varName) }

        if (scope != null) {
            val value = ctx.expression().accept(this)
            scope.assignVar(varName, value)
            return 1
        } else {
            throw Exception(formatErrOut(
                    ctx.IDENTIFIER().symbol.line,
                    ctx.IDENTIFIER().symbol.charPositionInLine,
                    "Can't find variable $varName"
            ))
        }
    }

    override fun visitUnitID(ctx: LangParser.UnitIDContext): Int? {
        val variableName = ctx.IDENTIFIER().text

        try {
            return stackScopes
                    .last { it.haveVar(variableName) }
                    .getVar(variableName)
        } catch (exc: NoSuchElementException) {
            throw Exception(formatErrOut(
                    ctx.IDENTIFIER().symbol.line,
                    ctx.IDENTIFIER().symbol.charPositionInLine,
                    "Can't find variable $variableName"
            ))
        }
    }

    override fun visitUnitLiteral(ctx: LangParser.UnitLiteralContext): Int? {
        val strLiteral = ctx.LITERAL().text
        return strLiteral.toInt()
    }

    override fun visitBoolExpr(ctx: LangParser.BoolExprContext): Int? {
        val leftVal: Int = ctx.expression(0).accept(this)
        val rightVal: Int = ctx.expression(1).accept(this)

        when (ctx.op.getType()) {
            AND -> return if (leftVal != 0 && rightVal != 0) 1 else 0
            OR -> return if (leftVal != 0 || rightVal != 0) 1 else 0
        }
        throw Exception(formatErrOut(
                ctx.op.line,
                ctx.op.charPositionInLine,
                "Don't support bool operation ${ctx.op.text}."
        ))
    }

    override fun visitComparisonExpr(ctx: ComparisonExprContext): Int {
        val leftVal: Int = ctx.expression(0).accept(this)
        val rightVal: Int = ctx.expression(1).accept(this)

        when (ctx.op.getType()) {
            EQUAL -> return if (leftVal == rightVal) 1 else 0
            NOTEQUAL -> return if (leftVal == rightVal) 0 else 1

            GE -> return if (leftVal >= rightVal) 1 else 0
            GT -> return if (leftVal > rightVal) 1 else 0
            LE -> return if (leftVal <= rightVal) 1 else 0
            LT -> return if (leftVal < rightVal) 1 else 0
         }

        throw Exception(formatErrOut(
                ctx.op.line,
                ctx.op.charPositionInLine,
                "Don't support comparison operation ${ctx.op.text}."
        ))
    }

    override fun visitAdditiveExpr(ctx: AdditiveExprContext): Int {
        val leftVal: Int = ctx.expression(0).accept(this)
        val rightVal: Int = ctx.expression(1).accept(this)

        when (ctx.op.getType()) {
            ADD -> return leftVal + rightVal
            SUB -> return leftVal - rightVal
        }

        throw Exception(formatErrOut(
                ctx.op.line,
                ctx.op.charPositionInLine,
                "Don't support additive operation ${ctx.op.text}."
        ))
    }

    override fun visitMultExpr(ctx: MultExprContext): Int {
        val leftVal: Int = ctx.expression(0).accept(this)
        val rightVal: Int = ctx.expression(1).accept(this)

        when (ctx.op.getType()) {
            MULT -> return leftVal * rightVal
            DIV -> return leftVal / rightVal
            MOD -> return leftVal % rightVal
        }

        throw Exception(formatErrOut(
                ctx.op.line,
                ctx.op.charPositionInLine,
                "Don't support multiplicative operation ${ctx.op.text}."
        ))
    }

    override fun visitIfExpression(ctx: IfExpressionContext): Int? {
        val conditionResult = ctx.condition.accept(this)

        if (conditionResult != 0) {
            return ctx.ifBody.accept(this)
        } else {
            val elseBody = ctx.elseBody
            if (elseBody != null) {
                return elseBody.accept(this)
            }
        }
        return null
    }

    override fun visitWhileExpression(ctx: WhileExpressionContext): Int {
        while (ctx.expression().accept(this) != 0) {
            ctx.bodyWithBraces().body().accept(this)
        }
        return 1
    }

    class Scope {
        private val varScope: MutableMap<String, Int?> = HashMap<String, Int?>()
        private val funcScope: MutableMap<String, LangParser.FunctionDeclarationContext> =
                HashMap<String, LangParser.FunctionDeclarationContext>()

        fun addVar(name: String) {
            varScope[name] = null
        }

        fun addVar(name: String, value: Int) {
            varScope[name] = value
        }

        fun assignVar(name: String, value: Int) {
            if (name in varScope.keys) {
                varScope[name] = value
            } else {
                throw Exception("Can't find variable $name")
            }
        }

        fun addFunc(name: String, ctx: LangParser.FunctionDeclarationContext) {
            funcScope[name] = ctx
        }

        fun haveVar(name: String): Boolean {
            return varScope.containsKey(name)
        }

        fun haveFunc(name: String): Boolean {
            return funcScope.containsKey(name)
        }

        fun getVar(name: String): Int? {
            return varScope[name]
        }

        fun getFunc(name: String): LangParser.FunctionDeclarationContext? {
            return funcScope[name]
        }
    }

    private fun formatErrOut(line: Int, position: Int, message: String): String {
        return "$line:$position: $message"
    }
}