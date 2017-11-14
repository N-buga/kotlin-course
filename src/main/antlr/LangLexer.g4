lexer grammar LangLexer;

IF:                 'if';
FUNCTION:           'fun';
VAR:                'var';
ELSE:               'else';
WHILE:              'while';
RETURN:             'return';
LITERAL:            ('0' | [1-9] (Digits?));

// Separators
LPAREN:             '(';
RPAREN:             ')';
LBRACE:             '{';
RBRACE:             '}';
COMMA:              ',';

// Whitespace and comments
LINE_COMMENT:       '//' ~[\r\n]*    -> channel(HIDDEN);
WS:                 (' ' | '\t' | '\n')+    -> channel(HIDDEN);

// Operators
ADD:                '+';
SUB:                '-';
MULT:               '*';
DIV:                '/';
MOD:                '%';
GT:                 '>';
LT:                 '<';
LE:                 '<=';
GE:                 '>=';
EQUAL:              '==';
NOTEQUAL:           '!=';
AND:                '&&';
OR:                 '||';

ASSIGN:             '=';

// Identifiers

IDENTIFIER:         Letter Letter*;

NL: '\u000A' | '\u000D' '\u000A' ;

fragment Letter: [a-zA-Z_];
fragment Digits
    : [0-9] ([0-9_]* [0-9])?
    ;
