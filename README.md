# CMMInterpreter
A C-Minus-Minus language interpreter based on LL1 grammar analysis
<br/>
### Supports:
#### Variable, array declaration
```java
int a = 10;
int[] b = {20, 10};
```
#### Value assignment
```java
a = b[0] + c;
```
#### Control logic
```java
int i, sum = 0;
for (i = 0; i < 10; i++) {
  if (i % 2 == 1)
    sum = sum + i * i;
}
```
#### Logic expressions
```java
while (i <= 2 || (a[i] < target && b[j] < target)) {
  if (a[i] < b[j])
    a[i] = a[i] + f[i];
  else b[j] = b[j] + f[j];
}
```

## Mechanism
1. Import rules from file, see 'rules.txt' or scroll to bottom
2. Generate grammar table
* Calculate FIRST, FOLLOW, SELECT tables
```
FIRST(assign_num_post_stm) : <LEFT_BRACKET> <ASSIGN> 
FIRST(array_post_stm) : 
FIRST(condition) : <INTEGER> <FALSE> <VAR_ID> <TRUE> <REAL> <LEFT_PAREN> 
FIRST(else_stm) : <ELSE> 
FIRST(op) : <EQUALS> <BIGGER_THAN> <MINUS> <OR> <ADD> <DIVIDE> <SMALLER_THAN> <NOTEQUAL> <NOT_BIGGER_THAN> <NOT_SMALLER_THAN> <TIMES> <AND> 
FIRST(declare_bool_post_stm) : <nil> <LEFT_BRACKET> <ASSIGN> 
FIRST(declare_stm) : <BOOL> <DOUBLE> <INT> 
FIRST(variable) : <VAR_ID> 
...
...
FOLLOW(body_stm) : <BOOL> <VAR_ID> <DOUBLE> <IF> <FOR> <RIGHT_BRACE> <WHILE> <INT> <hash> 
FOLLOW(op) : <INTEGER> <FALSE> <VAR_ID> <TRUE> <REAL> <LEFT_PAREN> 
FOLLOW(assign_num_post_stm) : <RIGHT_PAREN> <SEMI> 
FOLLOW(var_post_stm) : <MINUS> <SEMI> <RIGHT_BRACKET> <DIVIDE> <SMALLER_THAN> <EQUALS> <BIGGER_THAN> <RIGHT_PAREN> <OR> <ADD> <NOTEQUAL> <NOT_BIGGER_THAN> <NOT_SMALLER_THAN> <TIMES> <AND> 
FOLLOW(declare_bool_post_stm) : <SEMI> 
FOLLOW(for_stm) : <BOOL> <VAR_ID> <DOUBLE> <IF> <FOR> <RIGHT_BRACE> <WHILE> <INT> <hash> 
FOLLOW(declare_stm) : <BOOL> <VAR_ID> <DOUBLE> <IF> <FOR> <RIGHT_BRACE> <WHILE> <INT> <hash> 
FOLLOW(for_init_stm) : <SEMI> 
...
...
```
* Based on FIRST, FOLLOW sets generate symbol table:
```
...
var_post_stm : -><nil>(MINUS)	-><nil>(SEMI)	-><nil>(RIGHT_BRACKET)	-><nil>(DIVIDE)	-><nil>(SMALLER_THAN)	-><LEFT_BRACKET><expression><RIGHT_BRACKET>(LEFT_BRACKET)	-><nil>(EQUALS)	-><nil>(BIGGER_THAN)	-><nil>(RIGHT_PAREN)	-><nil>(OR)	-><nil>(ADD)	-><nil>(NOTEQUAL)	-><nil>(NOT_BIGGER_THAN)	-><nil>(NOT_SMALLER_THAN)	-><nil>(TIMES)	-><nil>(AND)	
declare_bool_post_stm : -><nil>(SEMI)	-><declare_array_stm>(LEFT_BRACKET)	-><ASSIGN><expression>(ASSIGN)	
declare_stm : -><BOOL><VAR_ID><declare_bool_post_stm><SEMI>(BOOL)	-><DOUBLE><VAR_ID><declare_num_post_stm><SEMI>(DOUBLE)	-><INT><VAR_ID><declare_num_post_stm><SEMI>(INT)	
for_stm : -><FOR><LEFT_PAREN><for_init_stm><SEMI><condition><SEMI><assign_stm><RIGHT_PAREN><body_stm>(FOR)	
for_init_stm : -><VAR_ID><ASSIGN><expression>(VAR_ID)	-><INT><VAR_ID><ASSIGN><expression>(INT)	
exp_post_stm : -><op><expression>(MINUS)	-><nil>(SEMI)	-><nil>(RIGHT_BRACKET)	-><op><expression>(DIVIDE)	-><op><expression>(SMALLER_THAN)	-><nil>(RIGHT_PAREN)	-><op><expression>(EQUALS)	-><op><expression>(BIGGER_THAN)	-><op><expression>(OR)	-><op><expression>(ADD)	-><op><expression>(NOTEQUAL)	-><op><expression>(NOT_BIGGER_THAN)	-><op><expression>(NOT_SMALLER_THAN)	-><op><expression>(TIMES)	-><op><expression>(AND)	
variable : -><VAR_ID><var_post_stm>(VAR_ID)	
...
```
3. Read and tokenize input code
```
(1,1) INT, int
(1,5) VAR_ID, i
(1,7) ASSIGN, =
(1,9) INTEGER, 0
(1,10) SEMI, ;
(2,1) INT, int
(2,5) VAR_ID, x
(2,7) ASSIGN, =
(2,9) INTEGER, 3
(2,10) SEMI, ;
(3,1) FOR, for
(3,5) LEFT_PAREN, (
(3,6) VAR_ID, i
(3,8) ASSIGN, =
(3,10) INTEGER, 0
(3,11) SEMI, ;
(3,13) VAR_ID, i
(3,15) SMALLER_THAN, <
(3,17) INTEGER, 10
(3,19) SEMI, ;
...
```
4. LL1 Grammar Analysis
* Analysis Stack
```
TopS = program, symbol = INT
USING PRODUCTION: program-><statement><program>
ANALYSIS STACK: hashprogramstatement
REMAINING INPUT: <INT><VAR_ID><ASSIGN><INTEGER><SEMI><INT><VAR_ID><ASSIGN><INTEGER><SEMI><FOR><LEFT_PAREN><VAR_ID><ASSIGN><INTEGER><SEMI><VAR_ID><SMALLER_THAN><INTEGER><SEMI><VAR_ID><ASSIGN><VAR_ID><ADD><INTEGER><RIGHT_PAREN><LEFT_BRACE><IF><LEFT_PAREN><VAR_ID><SMALLER_THAN><VAR_ID><DIVIDE><INTEGER><AND><VAR_ID><SMALLER_THAN><INTEGER><RIGHT_PAREN><VAR_ID><ASSIGN><VAR_ID><TIMES><LEFT_PAREN><VAR_ID><ADD><INTEGER><RIGHT_PAREN><SEMI><RIGHT_BRACE><hash>
TopS = statement, symbol = INT
USING PRODUCTION: statement-><declare_stm>
ANALYSIS STACK: hashprogramdeclare_stm
REMAINING INPUT: <INT><VAR_ID><ASSIGN><INTEGER><SEMI><INT><VAR_ID><ASSIGN><INTEGER><SEMI><FOR><LEFT_PAREN><VAR_ID><ASSIGN><INTEGER><SEMI><VAR_ID><SMALLER_THAN><INTEGER><SEMI><VAR_ID><ASSIGN><VAR_ID><ADD><INTEGER><RIGHT_PAREN><LEFT_BRACE><IF><LEFT_PAREN><VAR_ID><SMALLER_THAN><VAR_ID><DIVIDE><INTEGER><AND><VAR_ID><SMALLER_THAN><INTEGER><RIGHT_PAREN><VAR_ID><ASSIGN><VAR_ID><TIMES><LEFT_PAREN><VAR_ID><ADD><INTEGER><RIGHT_PAREN><SEMI><RIGHT_BRACE><hash>
TopS = declare_stm, symbol = INT
USING PRODUCTION: declare_stm-><INT><VAR_ID><declare_num_post_stm><SEMI>
ANALYSIS STACK: hashprogramSEMIdeclare_num_post_stmVAR_IDINT
REMAINING INPUT: <INT><VAR_ID><ASSIGN><INTEGER><SEMI><INT><VAR_ID><ASSIGN><INTEGER><SEMI><FOR><LEFT_PAREN><VAR_ID><ASSIGN><INTEGER><SEMI><VAR_ID><SMALLER_THAN><INTEGER><SEMI><VAR_ID><ASSIGN><VAR_ID><ADD><INTEGER><RIGHT_PAREN><LEFT_BRACE><IF><LEFT_PAREN><VAR_ID><SMALLER_THAN><VAR_ID><DIVIDE><INTEGER><AND><VAR_ID><SMALLER_THAN><INTEGER><RIGHT_PAREN><VAR_ID><ASSIGN><VAR_ID><TIMES><LEFT_PAREN><VAR_ID><ADD><INTEGER><RIGHT_PAREN><SEMI><RIGHT_BRACE><hash>
"INT" FOUND MATCH
REMAINING INPUT: hashprogramSEMIdeclare_num_post_stmVAR_ID
...
...
```
* LL1 Analysis Tree
```
program
-statement
 -declare_stm
  -INT(int)
  -VAR_ID(i)
  -declare_num_post_stm
   -ASSIGN(=)
   -expression
    -INTEGER(0)
    -exp_post_stm
     -nil(null)
  -SEMI(;)
-program
 -statement
  -declare_stm
   -INT(int)
   -VAR_ID(x)
   -declare_num_post_stm
    -ASSIGN(=)
    -expression
     -INTEGER(3)
     -exp_post_stm
      -nil(null)
   -SEMI(;)
...
...
```
5. Generate intermediate language, using quaternary productions (op, operand1, operand2, result)
```
(empty, null, null, i)
(=, *Temp0, null, i)
(empty, null, null, i)
(=, *Temp1, null, x)
(empty, null, null, i)
(=, *Temp2, null, i)
(<, i, 10.0, *Temp3)
(jmp, *Temp3, null, Q[19])
(/, x, 2.0, *Temp4)
(<, i, *Temp4, *Temp5)
(<, i, 4.0, *Temp6)
(&&, *Temp5, *Temp6, *Temp7)
(jmp, *Temp7, null, Q[16])
...
...
```
6. Run intermediate productions with interpreter, produce results
```
//input code:
int i = 0;
int x = 3;
for (i = 0; i < 10; i = i + 1) {
    if (i < x / 2 || i < 4)
        x = x * (i + 1);
}

//results:
Symbol Table:
Variable name = i, value = 10
Variable name = x, value = 10886400
```
## Rules
```
program->statement program
program->nil
statement->if_stm
statement->while_stm
statement->for_stm
statement->declare_stm
statement->assign_stm SEMI
sub_program->statement sub_program
sub_program->nil
body_stm->LEFT_BRACE sub_program RIGHT_BRACE
body_stm->statement
if_stm->IF LEFT_PAREN condition RIGHT_PAREN body_stm if_post_stm
if_post_stm->else_stm
if_post_stm->nil
else_stm->ELSE body_stm
condition->expression
assign_stm->VAR_ID assign_num_post_stm
assign_num_post_stm->ASSIGN expression
assign_num_post_stm->LEFT_BRACKET expression RIGHT_BRACKET ASSIGN expression
declare_stm->INT VAR_ID declare_num_post_stm SEMI
declare_stm->BOOL VAR_ID declare_bool_post_stm SEMI
declare_stm->DOUBLE VAR_ID declare_num_post_stm SEMI
declare_array_stm->LEFT_BRACKET INTEGER RIGHT_BRACKET
declare_num_post_stm->ASSIGN expression
declare_bool_post_stm->ASSIGN expression
declare_bool_post_stm->nil
declare_num_post_stm->nil
declare_bool_post_stm->declare_array_stm
declare_num_post_stm->declare_array_stm
op->ADD
op->MINUS
op->TIMES
op->DIVIDE
expression->REAL exp_post_stm
expression->INTEGER exp_post_stm
expression->variable exp_post_stm
variable->VAR_ID var_post_stm
var_post_stm->LEFT_BRACKET expression RIGHT_BRACKET
var_post_stm->nil
exp_post_stm->nil
exp_post_stm->op expression
expression->TRUE
expression->FALSE
expression->LEFT_PAREN expression RIGHT_PAREN
op->EQUALS
op->BIGGER_THAN
op->SMALLER_THAN
op->NOT_BIGGER_THAN
op->NOT_SMALLER_THAN
op->NOTEQUAL
op->AND
op->OR
for_stm->FOR LEFT_PAREN for_init_stm SEMI condition SEMI assign_stm RIGHT_PAREN body_stm
for_init_stm->INT VAR_ID ASSIGN expression
for_init_stm->VAR_ID ASSIGN expression
while_stm->WHILE LEFT_PAREN condition RIGHT_PAREN body_stm
```
