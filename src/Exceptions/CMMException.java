package Exceptions;

public class CMMException extends Exception{

    protected int line, column;
    public CMMException(int l, int c) {
        super();
        line = l;
        column = c;
    }

    @Override
    public String toString() {
        if (column > 0)
            return "At line " + line + ", column " + column + " : ";
        return "At line " + line + " : ";
    }
}
