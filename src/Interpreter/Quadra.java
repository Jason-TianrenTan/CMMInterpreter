package Interpreter;

public class Quadra {
    public int index = 0;
    public Object value;
    public Quadra(int i) {
        index = i;
    }

    @Override
    public String toString() {
        if (this.index == -1)
            return "" + value;
        return "[" + index + "]";
    }
}
