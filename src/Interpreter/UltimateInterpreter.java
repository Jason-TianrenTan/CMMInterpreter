package Interpreter;
import Analysis.Symbol;
import Exceptions.CMMException;
import Exceptions.NonNumericException;
import Exceptions.TypeUnmatchedException;
import SemanticAnalysis.SemanticSymbol;

import static SemanticAnalysis.SemanticSymbol.Kind;

import java.lang.reflect.Array;
import java.util.*;
public class UltimateInterpreter {

    private ArrayList<Quadron> QuadronList;
    private ArrayList<SemanticSymbol> SymbolTable;
    public UltimateInterpreter(ArrayList<Quadron> list, ArrayList<SemanticSymbol> symbols) {
        QuadronList = list;
        SymbolTable = symbols;
    }

    private int getInt(Object obj) {
        Double dVal = Double.parseDouble(obj.toString());
        return dVal.intValue();
    }

    private double getDouble(Object obj) {
        return Double.parseDouble(obj.toString());
    }

    public void execute() throws CMMException {
        for (int i=0;i<QuadronList.size();i++) {
            String op = QuadronList.get(i).getOperator();
            Quadra q1 = QuadronList.get(i).getArg1(), q2 = QuadronList.get(i).getArg2();
            int target = QuadronList.get(i).getTarget();
            //赋值
            if (op.equals("=")) {
                if (q1.index < 0)
                    SymbolTable.get(target).value = q1.value;
                else {
                    Object value = SymbolTable.get(q1.index).value;
                    if (q2 == null) {
                        if ((SymbolTable.get(target).type == Kind.Boolean) && (SymbolTable.get(q1.index).type == Kind.Boolean))
                            SymbolTable.get(target).value = value;
                        else if (((SymbolTable.get(target).type == Kind.Integer) || (SymbolTable.get(target).type == Kind.Real))
                            && ((SymbolTable.get(q1.index).type == Kind.Integer) || (SymbolTable.get(q1.index).type == Kind.Real))) {
                            Object tarV = null;
                            if (SymbolTable.get(target).type == Kind.Integer)
                                tarV = getInt(value);
                            else tarV = getDouble(value);
                            SymbolTable.get(target).value = tarV;
                        }
                        else
                            throw new TypeUnmatchedException(SymbolTable.get(target).line, SymbolTable.get(target).column,
                                    SymbolTable.get(target).type, SymbolTable.get(q1.index).type);

                    }
                    else {
                        Kind targetKind = SymbolTable.get(q1.index).type;
                        Object targetObj = SymbolTable.get(target).value;
                        if (((targetKind == Kind.ArrayBool) && (targetObj instanceof Boolean))
                         || ((targetKind == Kind.ArrayInt) && (targetObj instanceof Integer))
                         || ((targetKind == Kind.ArrayInt) && (targetObj instanceof Double))
                                || ((targetKind == Kind.ArrayReal) && (targetObj instanceof Double))) {
                            Object tarV = null;
                            if (targetKind == Kind.ArrayInt)
                                tarV = getInt(targetObj);
                            else if (targetKind == Kind.ArrayReal)
                                tarV = getDouble(targetObj);
                            Array.set(SymbolTable.get(q1.index).value, getInt(SymbolTable.get(q2.index).value), tarV);
                        } else {
                            Kind requiredType = null;
                            if (targetKind == Kind.ArrayBool)
                                requiredType = Kind.Boolean;
                            else if (targetKind == Kind.ArrayInt)
                                requiredType = Kind.Integer;
                            else if (targetKind == Kind.ArrayReal)
                                requiredType = Kind.Real;
                            throw new TypeUnmatchedException(SymbolTable.get(target).line, SymbolTable.get(target).column,
                                    requiredType, SymbolTable.get(target).type);
                        }
                    }
                }
            } else if (op.equals("give")) {
             //   System.out.println("Give " + SymbolTable.get(q1.index).value + " to " + SymbolTable.get(target).name);
                SemanticSymbol targetSymbol = SymbolTable.get(target), rSymbol = SymbolTable.get(q1.index);
                if (targetSymbol.type != rSymbol.type) {
                    throw new TypeUnmatchedException(targetSymbol.line, targetSymbol.column,
                            targetSymbol.type, rSymbol.type);
                }
                SymbolTable.get(target).value = SymbolTable.get(q1.index).value;
            }
            //运算符
            else if (op.equals("+") || op.equals("*") || op.equals("-") || op.equals("/")) {
                double arg1 = 0, arg2 = 0;
                try {
                    if (q1.index < 0)
                        arg1 = Double.parseDouble(q1.value.toString());
                    else
                        arg1 = Double.parseDouble(SymbolTable.get(q1.index).value.toString());
                    if (q2.index < 0)
                        arg2 = Double.parseDouble(q2.value.toString());
                    else
                        arg2 = Double.parseDouble(SymbolTable.get(q2.index).value.toString());
                } catch (Exception e) {
                    throw new NonNumericException(SymbolTable.get(q1.index).line, SymbolTable.get(q2.index).column);
                }
                if (op.equals("+"))
                    SymbolTable.get(target).value = arg1 + arg2;
                else if (op.equals("-"))
                    SymbolTable.get(target).value = arg1 - arg2;
                else if (op.equals("*"))
                    SymbolTable.get(target).value = arg1 * arg2;
                else if (op.equals("/"))
                    SymbolTable.get(target).value = arg1 / arg2;
            }
            else if (op.equals("||") || op.equals("&&")) {
                boolean arg1 = false, arg2 = false;
                if (q1.index < 0)
                    arg1 = (boolean)q1.value;
                else arg1 = (boolean)SymbolTable.get(q1.index).value;
                if (q2.index < 0)
                    arg2 = (boolean)q2.value;
                else arg2 = (boolean)SymbolTable.get(q2.index).value;
                if (op.equals("||"))
                    SymbolTable.get(target).value = arg1 || arg2;
                else if (op.equals("&&"))
                    SymbolTable.get(target).value = arg1 && arg2;
            } else if (op.equals(">") || op.equals("<") || op.equals(">=") || op.equals("<=")) {
                double arg1 = 0, arg2 = 0;
                if (q1.index < 0)
                    arg1 = Double.parseDouble(q1.value.toString());
                else
                    arg1 = Double.parseDouble(SymbolTable.get(q1.index).value.toString());
                if (q2.index < 0)
                    arg2 = Double.parseDouble(q2.value.toString());
                else
                    arg2 = Double.parseDouble(SymbolTable.get(q2.index).value.toString());
                if (op.equals(">"))
                    SymbolTable.get(target).value = (arg1 > arg2);
                else if (op.equals("<"))
                    SymbolTable.get(target).value = (arg1 < arg2);
                else if (op.equals(">="))
                    SymbolTable.get(target).value = (arg1 >= arg2);
                else if (op.equals("<="))
                    SymbolTable.get(target).value = (arg1 <= arg2);
            }
            //特殊，等于判断
            else if (op.equals("==")) {
                Object arg1, arg2;
                if (q1.index < 0)
                    arg1 = q1.value;
                else arg1 = SymbolTable.get(q1.index).value;
                if (q2.index < 0)
                    arg2 = q2.value;
                else arg2 = SymbolTable.get(q2.index).value;
                if (SymbolTable.get(q1.index).type == Kind.Integer) {
                    int val1 = (int)arg1, val2 = (int)arg2;
                    SymbolTable.get(target).value = val1 == val2;
                } else if (SymbolTable.get(q1.index).type == Kind.Real) {
                    double val1 = (double)arg1, val2 = (double)arg2;
                    SymbolTable.get(target).value = val1 == val2;
                } else if (SymbolTable.get(q1.index).type == Kind.Boolean) {
                    boolean val1 = (boolean)arg1, val2 = (boolean)arg2;
                    SymbolTable.get(target).value = val1 == val2;
                }
            }
            //跳转
            else if (op.equals("jmp")) {
                boolean flag = true;
                if (q1.index < 0)
                    flag = (boolean)q1.value;
                else flag = (boolean)SymbolTable.get(q1.index).value;
                if (!flag) {
                    //jump
                    i = target - 1;
                    //System.out.println("jump to " + target);
                }
            }
            //数组取元素值
            else if (op.equals("^")) {
                Object array = SymbolTable.get(q1.index).value;
                int qindex = -1;
                if (q2.index < 0) {
                    qindex = getInt(q2.value.toString());
                }
                else {
                    qindex = getInt(SymbolTable.get(q2.index).value);
                }
                SymbolTable.get(target).value = Array.get(array, qindex);
            }
        }

        System.out.println("Symbol Table:");
        for (SemanticSymbol ss : SymbolTable) {
            if (!ss.name.startsWith("*Temp")) {
                if ((ss.type != Kind.ArrayBool) && (ss.type != Kind.ArrayInt) && (ss.type != Kind.ArrayReal))
                    System.out.println("Variable name = " + ss.name + ", value = " + ss.value);
                else {
                    System.out.print("Array: " + ss.name + "[" + ss.size + "] {");
                    for (int i=0;i<ss.size;i++) {
                        System.out.print(Array.get(ss.value, i));
                        if (i < ss.size - 1)
                            System.out.print(",");
                    }
                    System.out.println("}");
                }
            }
        }
    }
}
