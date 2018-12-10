package SemanticAnalysis;

public class SemanticSymbol {
    public String name;
    public enum Kind { Integer, Real, Boolean, ArrayInt, ArrayReal, ArrayBool, Procedure};
    public int level;
    public int size;
    public int line, column;
    public Object value = null;
    public Kind type;

    public SemanticSymbol(String str, Kind kind, int clevel, int l, int c) {
        this.name = str;
        this.type = kind;
        this.level = clevel;
        this.line = l;
        this.column = c;
    }

    public boolean isNumeric() {
        return (this.type == Kind.Integer) || (this.type == Kind.Real);
    }

    public boolean isLogical() {
        return this.type == Kind.Boolean;
    }
}
