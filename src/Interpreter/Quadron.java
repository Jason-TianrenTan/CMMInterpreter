package Interpreter;

public class Quadron {

    //并非最终四元式 只是中间生成的辅助类
    private String operator;//第一项
    private Quadra arg1;

    public String getOperator() {
        return operator;
    }

    public Quadra getArg1() {
        return arg1;
    }

    public Quadra getArg2() {
        return arg2;
    }

    public int getTarget() {
        return target;
    }

    private Quadra arg2;
    private int target;//
    public Quadron(String op, Quadra a1, Quadra a2, int trg) {
        operator = op;
        arg1 = a1;
        arg2 = a2;
        target = trg;
    }

    @Override
    public String toString() {
        return "(" + operator + ", " + arg1 + ", " + arg2 + ", [" + target + "])";
    }

    public void setJump(int tar) {
        if (this.operator.equals("jmp"))
            this.target = tar;
    }

    /**
     * 操作符号 [op], arg1, arg2, target
     * 赋值 =, arg1, null, target
     * 数组元素赋值 =, arg1, arg2(=index), target //反着的
     * 跳转 jmp, arg1, null, target //注意：只有这里target是指四元式下标, 如果arg1=false则进行跳转
     * 比较 [comp_op], arg1, arg2, target
     * 补位 empty,null,null,null
     */


}
