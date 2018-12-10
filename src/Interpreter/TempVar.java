package Interpreter;

public class TempVar {

    private TempType type;
    private Object value;
    public TempVar(int val) {
        type = TempType.Integer;
        value = val;
    }

    public TempVar(boolean flag) {
        type = TempType.Boolean;
        value = flag;
    }

    public TempType getType() {
        return this.type;
    }

    public boolean isInteger() {
        return this.type == TempType.Integer;
    }

    public boolean isBoolean() {
        return this.type == TempType.Boolean;
    }

    public Object getValue() {
        return this.value;
    }
}
