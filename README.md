# Canterbury

## Grammar

```
program       ->  stmt* EOF;
stmt          ->  exprStmt
                  | forStmt
                  | ifStmt
                  | whileStmt
                  | matchStmt;

exprStmt      ->  expr;
forStmt       ->  "for" "[" ( forOf | forUntil ) "]" block;
forOf         ->  IDENTIFIER "," call;
forUntil      ->  var_assign "," logicOr? "," logicOr?;
ifStmt        ->  "if" "[" expr "]" block else?;
else          ->  ( "elif" block )? "else" block;
whileStmt     ->  "while" "[" expr "]" stmt;
matchStmt     ->  "match" "[" logic_or "]" "against" ( case+ );
block         ->  "[" stmt* "]";

expr          ->  assignment | logicOr;
assignment    ->  var_assign | metaAssign;
varAssign     ->  IDENTIFIER ":=" logicOr;
metaAssign    ->  IDENTIFIER ":=" ( classAssign | structAssign | funcAssign );
classAssign   ->  "class" IDENTIFIER "[" params? "]" "[" assignment* "]";
structAssign  ->  "struct" IDENTIFIER "[" params? "]";
funcAssign    ->  ("func" | "lambda" | "λ" ) IDENTIFIER "[" params? "]" block;

logicOr       ->  logic_And ( ( "or" | "|" ) logicAnd )*;
logicAnd      ->  equality ( ( "and" | "&" ) equality )*;
equality      ->  comparison ( ( "is" | "=" | "<>" ) comparison )*;
comparison    ->  term ( ( ">" | ">=" | "<" | "<=" ) term )*;
term          ->  factor ( ( "-" | "+" ) factor )*;
factor        ->  unary ( ( "/" | "*" ) unary )*;
unary         ->  ( "!" | "-" ) unary | call;

call          ->  primary ( "[" args? "]" | IDENTIFIER )*;
primary       ->  selector | "this" | expr | super IDENTIFIER;
selector      ->  "true" | "false" | "null"
                  | NUM | STR | LIST | MAP | IDENTIFIER

case          ->  "[" ( selector | "_" ) "->" ( expr | block ) "]";
params        ->  IDENTIFIER ( "," IDENTIFIER )*;
args          ->  expr ( "," expr )*;

NUM           ->  BIN_NUM | HEX_NUM | DEC_NUM;
BIN_NUM       ->  "0b" BIN_DIGIT+;
HEX_NUM       ->  "0x" HEX_DIGIT+;
DEC_NUM       ->  DEC_DIGIT+ ( "." DEC_DIGIT+ )?
STR           ->  ( "\'" <any char except "\'"* "\'" )
                  | ( "\"" <any char except "\'"* "\"" );
LIST          ->  "(" ( ( expr "," )? expr ) | expr? ")";
MAP           ->  "{" ( ( MAP_ENTRY "," )? MAP_ENTRY ) | MAP_ENTRY? "}";
MAP_ENTRY     ->  IDENTIFIER ":" expr;
IDENTIFIER    ->  ALPHA ( ALPHA | DEC_DIGIT )*;
ALPHA         ->  "A" ... "Z" | "a" ... "z" | "_";
BIN_DIGIT     ->  "0" | "1";
DEC_DIGIT     ->  "0" ... "9";
HEX_DIGIT     ->  DEC_DIGIT | "A" ... "F" | "a" ... "f";
```
