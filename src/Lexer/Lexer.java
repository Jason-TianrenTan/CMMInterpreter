package Lexer;
import Analysis.TerminalS;

import javax.sound.sampled.Line;
import java.io.*;
import java.util.*;

import static Lexer.Config.State;
import static Lexer.Token.Type;
import static Lexer.Config.IDentifierMap;

public class Lexer {

    File targetFile;
    BufferedReader reader;
    int line = 1, column = 1, pointer = 0;
    int start_pos = 0, token_pos = 0;
    String word = "";
    char[] document;
    State currentState = State.S;
    ArrayList<Token> tokenList;

    private void initIdentifiers() {
        IDentifierMap = new HashMap<String, Type>();
        IDentifierMap.put("int", Type.INT);
        IDentifierMap.put("double", Type.DOUBLE);
        IDentifierMap.put("float", Type.FLOAT);
        IDentifierMap.put("bool", Type.BOOL);
        IDentifierMap.put("if", Type.IF);
        IDentifierMap.put("else", Type.ELSE);
        IDentifierMap.put("for", Type.FOR);
        IDentifierMap.put("while", Type.WHILE);
    }

    public Lexer(String filepath) {
        tokenList = new ArrayList<>();
        initIdentifiers();
        try {
            targetFile = new File(filepath);
            reader = new BufferedReader(new FileReader(targetFile));
            String currentLine = null;
            String doc_str = "";
            while ((currentLine = reader.readLine()) != null) {
                doc_str += currentLine + "\n";
            }
            document = doc_str.toCharArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Token> startParse() {
        try {
            while (pointer < document.length) {
                if (document[pointer] == '\n') {
                    line++;
                    System.out.println();
                    column = 1;
                } else {
                    start_pos = pointer;//记录起始位置
                    if ((document[pointer] != '\t') && (document[pointer] != ' ')) {
                        if (currentState == State.S) {
                            Parse();
                        } else if (currentState == State.MULTI_ANNO) {
                            toMultiLineAnnotation();
                        }
                    }
                    int increment = pointer - start_pos;
                    column += increment;

                    column++;
                }
                pointer++;

            }


            System.out.println("\nTokens:");
            for (Token token : tokenList) {
                System.out.println("(" + token.getLine() + "," + token.getColumn()+") " +
                        token.getType() + ", " + token.getContent());
                String name = token.getType().name();
                token.symbol = TerminalS.valueOf(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tokenList;
    }


    private void Parse() {
        if (document[pointer] == '/') {
            toAnnotation();
        } else if (isLetter(document[pointer])) {
            toIdentifier();
        } else if (isDigit(document[pointer])) {
            toNumber();
        } else if (document[pointer] == '=') {
            if (pointer < document.length && document[pointer + 1] == '=') {
                tokenList.add(new Token(Type.EQUALS, "==", line, column));
                word = "";
                currentState = State.S;
            } else {
                tokenList.add(new Token(Type.ASSIGN, "=", line, column));
                word = "";
                currentState = State.S;
            }
        } else if (document[pointer] == '>' || document[pointer] == '<') {
            if (pointer < document.length) {
                word += document[pointer];
                if ((document[pointer + 1] == '=') || (document[pointer] == '<' && document[pointer + 1] == '>')) {
                    word += document[pointer + 1];
                    pointer++;
                    Type compType;
                    if (word.equals("<>"))
                        compType = Type.NOTEQUAL;
                    else if (word.equals("<="))
                        compType = Type.NOT_BIGGER_THAN;
                    else compType = Type.NOT_SMALLER_THAN;
                    tokenList.add(new Token(compType, word, line, column));
                    word = "";
                    currentState = State.S;
                    return;
                }
            }
            Type compType = Type.SMALLER_THAN;
            if (word.equals(">"))
                compType = Type.BIGGER_THAN;
            tokenList.add(new Token(compType, word, line, column));
            word = "";
            pointer++;
            currentState = State.S;
        } else if (document[pointer] == '+') {
            toPlus();
        } else if (document[pointer] == '-') {
            toMinus();
        } else if (document[pointer] == '*') {
            toMulti();
        } else if (document[pointer] == '{' || document[pointer] == '}') {
            Type braceType = document[pointer] == '{' ? Type.LEFT_BRACE : Type.RIGHT_BRACE;
            Token token = new Token(braceType, document[pointer] + "", line, column);
            tokenList.add(token);
            word = "";
            currentState = State.S;
        } else if (document[pointer] == '[' || document[pointer] == ']') {
            Type bracketType = document[pointer] == '[' ? Type.LEFT_BRACKET : Type.RIGHT_BRACKET;
            Token token = new Token(bracketType, document[pointer] + "", line, column);
            tokenList.add(token);
            word = "";
            currentState = State.S;
        } else if (document[pointer] == '(' || document[pointer] == ')') {
            Type parenType = document[pointer] == '(' ? Type.LEFT_PAREN : Type.RIGHT_PAREN;
            Token token = new Token(parenType, document[pointer] + "", line, column);
            tokenList.add(token);
            word = "";
            currentState = State.S;
        } else if (document[pointer] == ';') {
            Token token = new Token(Type.SEMI, ";", line, column);
            tokenList.add(token);
            word = "";
            currentState = State.S;
        } else if (document[pointer] == '&') {
            if ((pointer < document.length) && (document[pointer + 1] == '&')) {
                tokenList.add(new Token(Type.AND, "&&", line, column));
            } else {
                System.out.println("Error symbol : \'&\'");
            }
        } else if (document[pointer] == '|') {
            if ((pointer < document.length) && (document[pointer + 1] == '|')) {
                tokenList.add(new Token(Type.OR, "||", line, column));
            } else {
                System.out.println("Error symbol : \'|\'");
            }
        } else {
            System.out.println("Unable to recognize token at line " +
                line + ", column " + column + ": \'" + document[pointer] + "\'");
        }
    }

    private void toMulti() {
        if (pointer < document.length) {
            if (document[pointer + 1] == '=')
            {
                tokenList.add(new Token(Type.INCMULTI, "*=", line, column));
                currentState = State.S;
                pointer++;
                word = "";
                return;
            }
        }
        tokenList.add(new Token(Type.TIMES, "*", line, column));
        currentState = State.S;
        word = "";
    }

    private void toPlus() {
        if (pointer < document.length) {
            if (document[pointer + 1] == '+')
            {
                tokenList.add(new Token(Type.PLUSPLUS, "++", line, column));
                currentState = State.S;
                pointer++;
                word = "";
                return;
            }
            if (document[pointer + 1] == '=')
            {
                tokenList.add(new Token(Type.INCREMENT, "+=", line, column));
                currentState = State.S;
                pointer++;
                word = "";
                return;
            }
        }
        tokenList.add(new Token(Type.ADD, "+", line, column));
        currentState = State.S;
        word = "";
    }

    private void toMinus() {
        if (pointer < document.length) {
            if (document[pointer + 1] == '-')
            {
                tokenList.add(new Token(Type.MINUSMINUS, "--", line, column));
                currentState = State.S;
                pointer++;
                word = "";
                return;
            }
            if (document[pointer + 1] == '=')
            {
                tokenList.add(new Token(Type.DECREMENT, "-=", line, column));
                currentState = State.S;
                pointer++;
                word = "";
                return;
            }
        }
        tokenList.add(new Token(Type.MINUS, "-", line, column));
        currentState = State.S;
        word = "";
    }

    private void toNumber() {
        char start = document[pointer];
        word += document[pointer];
        pointer++;
        while (isDigit(document[pointer])) {
            word += document[pointer];
            pointer++;
        }
        if (word.length() > 1 && start == '0') {
            System.out.println("Error at line " + line + ", column " + column + ": \'" +
                    word + "\' number starts with 0");
        }
        if (document[pointer] == '.') {
            word += '.';
            pointer++;
            int flLength = 0;
            if (pointer < document.length) {
                while (isDigit(document[pointer])) {
                    word += document[pointer];
                    pointer++;
                    flLength++;
                }
                if (flLength > 0) {
                    tokenList.add(new Token(Type.REAL, word, line, column));
                    word = "";
                    currentState = State.S;
                    pointer--;
                    return;
                } else {
                    System.out.println("Error at line " + line + ", column " + column + "\'" +
                            word + "\':  float not correctly ended");
                    currentState = State.S;
                    pointer--;
                }
            }
        } else {
            //integer
            tokenList.add(new Token(Type.INTEGER, word, line, column));
            word = "";
            currentState = State.S;
            pointer--;
            return;
        }
    }


    private void toIdentifier() {
        word += document[pointer];
        pointer++;
        while (isLetter(document[pointer]) || isDigit(document[pointer]) || document[pointer] == '_') {
            word += document[pointer];
            pointer++;
        }

        //判断关键字
        Iterator iter = IDentifierMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Type> entry = (Map.Entry<String, Type>)iter.next();
            String idName = entry.getKey();
            Type type = entry.getValue();
            if (idName.equals(word)) {
                tokenList.add(new Token(type, idName, line, column));
                word = "";
                currentState = State.S;
                return;
            }
        }

        //不是关键字有可能是true或者false
        if (word.equals("true")) {
            tokenList.add(new Token(Type.TRUE, word, line, column));
        } else if (word.equals("false")) {
            tokenList.add(new Token(Type.FALSE, word, line, column));
        } else {
            tokenList.add(new Token(Type.VAR_ID, word, line, column));
        }
        currentState = State.S;
        word = "";
        pointer--;

    }


    /**
     * 注释（单行，多行）,除号
     */
    private void toAnnotation() {
        word = "/";
        if (pointer < document.length) {
            pointer++;
            if (document[pointer] == '*')//多行注释
            {
                word = "/*";
                pointer++;
                currentState = State.MULTI_ANNO;
                toMultiLineAnnotation();
                return;
            } else if (document[pointer] == '/') {
                while (document[pointer] != '\n') {
                    word += document[pointer];
                    pointer++;
                }
                tokenList.add(new Token(Type.SINGLE_ANNOTATION, word, line, column));
                currentState = State.S;
                pointer--;
                word = "";
                return;
            } else if (document[pointer] == '=') {
                tokenList.add(new Token(Type.DEDIVIDE, "/", line, column));
                word = "";
                currentState = State.S;
                return;
            }
        }

        tokenList.add(new Token(Type.DIVIDE, "/", line, column));
        pointer--;
        word = "";
        currentState = State.S;
    }


    private void toMultiLineAnnotation() {
        int startLine = line;
        for (; pointer < document.length; pointer++) {
            word += document[pointer];
            if (document[pointer] == '\n')
            {
                line++;
                column = 1;
            }
            if (document[pointer] == '*') {
                if ((pointer < document.length - 1)
                        && document[pointer + 1] == '/') {
                    word += "/";
                    tokenList.add(new Token(Type.MULTI_ANNOTATION, word, startLine, column));
                    pointer++;
                    word = "";
                    currentState = State.S;
                    return;
                }
            }
        }

        System.out.println("Error, multi annotation not correctly closed");
    }


    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
