parser grammar LangParser;

options { tokenVocab=LangLexer; }

file
    : body EOF
    ;

body
    : (statement)*
    ;

bodyWithBraces
    : LBRACE body RBRACE
    ;

statement
    : functionDeclaration
    | assigment
    | variableDeclaration
    | expression
    | whileExpression
    | ifExpression
    | returnExpression
    ;

functionDeclaration
    : FUNCTION IDENTIFIER LPAREN parameterNames RPAREN bodyWithBraces
    ;

variableDeclaration
    : VAR IDENTIFIER (ASSIGN expression)?
    ;

parameterNames
    : IDENTIFIER?
    | IDENTIFIER (COMMA IDENTIFIER)*
    ;

whileExpression
    : WHILE LPAREN expression RPAREN bodyWithBraces
    ;

ifExpression
    : IF LPAREN condition=expression RPAREN ifBody=bodyWithBraces (ELSE elseBody=bodyWithBraces)?
    ;

assigment
    : IDENTIFIER ASSIGN expression
    ;

returnExpression
    : RETURN expression
    ;

expression
    : functionCall                              # functionCallExpr
    | operationUnit                             # operationUnitExpr
    | LPAREN expression RPAREN                  # paranthesisExpr
    | expression op=(MULT | DIV | MOD) expression # multExpr
    | expression op=(ADD | SUB) expression      # additiveExpr
    | expression op=(EQUAL | NOTEQUAL | LE | GE | LT | GT) expression # comparisonExpr
    | expression op=(AND | OR) expression       # boolExpr
    ;

functionCall
    : IDENTIFIER LPAREN arguments RPAREN
    ;

arguments
    : expression?
    | expression (COMMA expression)*
    ;

operationUnit
    : IDENTIFIER  # unitID
    | LITERAL     # unitLiteral
    ;
