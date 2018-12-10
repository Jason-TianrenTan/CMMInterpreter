package Lexer;
import java.util.*;
import static Lexer.Token.Type;
public class Config {

    public static enum State{
        S, CHAR, NUMBER, MULTI_ANNO
    };

    public static HashMap<String, Type> IDentifierMap;
}
