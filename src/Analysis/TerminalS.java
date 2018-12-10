package Analysis;

public enum TerminalS implements Symbol {


    nil, hash, IF, ELSE, WHILE, FOR, LEFT_BRACE, RIGHT_BRACE,
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACKET, RIGHT_BRACKET, TRUE, FALSE,
    INT, DOUBLE, BOOL, VAR_ID, ASSIGN, ADD, MINUS, TIMES, DIVIDE,
    REAL, INTEGER, SEMI,
    AND, OR, COMMA,
    EQUALS, BIGGER_THAN, SMALLER_THAN, NOT_BIGGER_THAN, NOT_SMALLER_THAN, NOTEQUAL,
    PLUSPLUS, MINUSMINUS, INC_ADD, INC_MINUS, INC_MULTI, INC_DIVIDE;

    //i, LEFTP, RIGHTP, nil, hash, plus, times;
    //private Token token;
    @Override
    public boolean isTerminal() {
        return true;
    }
}
