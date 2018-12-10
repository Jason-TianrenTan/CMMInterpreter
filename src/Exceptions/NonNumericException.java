package Exceptions;

public class NonNumericException extends CMMException{

    private int line, column;
    public NonNumericException(int l, int c) {
        super(l, c);
        line = l;
        column = c;
    }

    @Override
    public String toString() {
        return super.toString() + "Numeric type required";
    }
}
