package Lexer;

import Analysis.Symbol;

public class Token {


    /*
        运算符 +,-,*,/,%,=
        注释 //
        分号 ;
        定义 int,double,float,bool
        括号 ()
        花括号 {}
         */
    public static enum Type {
        INT, DOUBLE, FLOAT, BOOL,
        INTEGER, REAL,//整数，浮点数
        TRUE, FALSE,//true, false
        ADD, MINUS, TIMES, DIVIDE,//+, -, *, /
        ASSIGN,//=
        SEMI,//;
        SINGLE_ANNOTATION,// //
        MULTI_ANNOTATION,// /*
        LEFT_BRACE, RIGHT_BRACE,//花括号
        LEFT_BRACKET, RIGHT_BRACKET,//方括号
        LEFT_PAREN, RIGHT_PAREN,//小括号
        BIGGER_THAN, SMALLER_THAN, NOT_BIGGER_THAN, NOT_SMALLER_THAN, NOTEQUAL, EQUALS,//大于，小于，大于等于，小于等于，不等于, 等于
        PLUSPLUS, MINUSMINUS,//++,--
        INCREMENT, DECREMENT,//+=, -=
        INCMULTI, DEDIVIDE,// *=, /=
        AND, OR,//&&, ||
        IF, ELSE, FOR, WHILE,
        VAR_ID
    }
    private Type type;//Token类型
    private String content;//内容
    private int line, column;//所在行列
    public Symbol symbol;

    public Token(Type type, String content, int l, int c) {
        this.type = type;
        this.content = content;
        line = l;
        column = c;
        System.out.print(type + " ");
    }

    public Type getType() {
        return type;
    }

    public void setType(String Type) {
        this.type = type;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }
}
