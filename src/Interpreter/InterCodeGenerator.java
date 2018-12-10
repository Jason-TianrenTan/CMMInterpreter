package Interpreter;

import Analysis.NTerminalS;
import Analysis.Symbol;
import Analysis.TerminalS;
import Analysis.TreeNode;
import Exceptions.CMMException;
import Exceptions.TypeUnmatchedException;
import SemanticAnalysis.SemanticSymbol;

import static SemanticAnalysis.SemanticSymbol.Kind;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InterCodeGenerator {

    /**
     * 基本思路：遍历 生成中间代码
     * 遇到变量先忽略，后续执行的时候直接从符号表调出值
     */
    private ArrayList<Quadron> QuadronList;//四元式列表
    private ArrayList<SemanticSymbol> SymbolTable;
    private TreeNode SyntaxRoot;
    private ArrayList<TempVar> TempList;//临时变量
    private int TempCount = 0;
    public InterCodeGenerator(ArrayList<SemanticSymbol> table, TreeNode syntaxRoot) {
        SymbolTable = table;
        SyntaxRoot = syntaxRoot;
        TempList = new ArrayList<>();
        QuadronList = new ArrayList<>();
    }

    //判断符号表是否已经存在
    private int findVariable(String varID, int currentLevel) {
        int maxLevel = -1;
        int bestIndex = -1;
        for (int i=0;i<SymbolTable.size();i++) {
            if (SymbolTable.get(i).name.equals(varID)) {
                if (SymbolTable.get(i).level <= currentLevel && SymbolTable.get(i).level >= maxLevel) {
                    maxLevel = SymbolTable.get(i).level;
                    bestIndex = i;
                }
            }
        }
        return bestIndex;
    }

    public void GenerateCode() throws CMMException {
        dfs(SyntaxRoot, 0);
        System.out.println("Symbol Table : ");
        for (SemanticSymbol ss : SymbolTable) {
            System.out.print(ss.name);
            if (ss.value != null)
                System.out.print("(" + ss.value + ")");
            System.out.print(" ");
        }
        System.out.println();
        for (Quadron quadron : QuadronList) {
            PrintQuadron(quadron);
        }
        new UltimateInterpreter(QuadronList, SymbolTable).execute();
    }

    private void PrintQuadron(Quadron quadron) {
        String arg1 = "", arg2 = "";
        if (quadron.getArg1() != null) {
            int i1 = quadron.getArg1().index;
            if (i1 < 0)
                arg1 = "" + quadron.getArg1().value;
            else
                arg1 = SymbolTable.get(i1).name;
        } else arg1 = "null";
        if (quadron.getArg2() != null) {
            int i2 = quadron.getArg2().index;
            if (i2 < 0)
                arg2 = "" + quadron.getArg2().value;
            else
                arg2 = SymbolTable.get(i2).name;
        } else arg2 = "null";

        String arg3 = "";
        if (quadron.getOperator().equals("jmp"))
            arg3 = "Q[" + quadron.getTarget() + "]";
        else arg3 = SymbolTable.get(quadron.getTarget()).name;
        String str = "(" + quadron.getOperator() + ", " + arg1
                + ", " + arg2 + ", " + arg3 + ")";
        System.out.println(str);
    }


    //数字运算栈
    private int top = -1;
    private String[] Stack = new String[10000];

    private void pop() {
        top--;
    }

    private void push(String op) {
        top++;
        Stack[top] = op;
    }

    private void clearStack() {
        top = -1;
        STop = -1;
    }

    //符号栈
    private String[] SymbolStack = new String[10000];
    private int STop = -1;
    private void SPop() {
        STop--;
    }

    private void SPush(String symbol) {
        STop++;
        SymbolStack[STop] = symbol;
    }

    private ArrayList<String> expandExp(TreeNode expression, int codeLevel) {
        TreeNode valueNode = expression.Children[0];
        ArrayList<String> ret = new ArrayList<>();
        if (valueNode.symbol == TerminalS.INTEGER
            || valueNode.symbol == TerminalS.REAL
            || valueNode.symbol == TerminalS.TRUE
            || valueNode.symbol == TerminalS.FALSE)
            ret.add(valueNode.name);
        else if (valueNode.symbol == NTerminalS.variable) {
            //变量
            String innerVarID = valueNode.Children[0].name;
            TreeNode valPostNode = valueNode.Children[1];
            if (valPostNode.Children.length > 1) {
                //数组元素
                TreeNode indexExpression = valPostNode.Children[1];
                ArrayList<String> indexExpand = expandExp(indexExpression, codeLevel);
                ret.add(innerVarID);//^代表求数组运算
                ret.add("^");
                ret.add("(");
                for (int i=0;i<indexExpand.size();i++) {
                    ret.add(indexExpand.get(i));
                }
                ret.add(")");
            }
            else {
                //单变量
                ret.add(innerVarID);
            }
        }
        if (expression.Children.length > 1) {

            if (expression.Children[0].symbol == TerminalS.LEFT_PAREN) {
                ret.add("(");
                TreeNode inner_expr = expression.Children[1];
                ArrayList<String> innerExpand = expandExp(inner_expr, codeLevel);
                for (String str :innerExpand)
                    ret.add(str);
                ret.add(")");
            }
            else {
                TreeNode PostStm = expression.Children[1];
                if (PostStm.Children[0].symbol == TerminalS.nil)//终结
                    return ret;
                String operator = PostStm.Children[0].Children[0].name;
                ret.add(operator);
                ArrayList<String> postExpand = expandExp(PostStm.Children[1], codeLevel);
                for (String str : postExpand)
                    ret.add(str);
            }
        }
        return ret;
    }

    private static boolean isNumber(String str) {
        String reg = "^[0-9]+(.[0-9]+)?$";
        return str.matches(reg);
    }


    private int isOperator(String str) {
        if (str.equals("&&") || str.equals("||"))
            return 1;
        if (str.equals("==") || str.equals("!="))
            return 2;
        if (str.equals(">") || str.equals("<") || str.equals("<=") || str.equals(">="))
            return 3;
        if (str.equals("+") || str.equals("-"))
            return 4;
        if (str.equals("*") || str.equals("/"))
            return 5;
        if (str.equals("^"))
            return 6;
        if (str.equals("(") || str.equals(")"))
            return 7;
        return -1;
    }

    private void InterpretExpStm(TreeNode expression, Kind varType, int codeLevel, int line, int column) {
        //展开表达式
        clearStack();
        ArrayList<String> output = new ArrayList<>();
        ArrayList<String> result = expandExp(expression, codeLevel);
        for (String str : result) {
            int op = isOperator(str);
            if (op > 0) {
                //符号
                if (top < 0)
                    push(str);
                else {
                    //先判断括号
                    if (str.equals(")")) {
                        while (top >= 0 && (!Stack[top].equals("("))) {
                            output.add(Stack[top]);
                            pop();
                        }
                        pop();
                    }
                    else {
                        if (isOperator(Stack[top]) < op)
                            push(str);
                        else {
                            while (top >= 0 && (isOperator(Stack[top]) >= op) && !Stack[top].equals("(")) {
                                output.add(Stack[top]);
                                pop();
                            }
                            push(str);
                        }
                    }
                }
            } else {
                //数字、变量
                output.add(str);
            }
        }
        System.out.println();
        while (top >= 0) {
            if (!Stack[top].equals("(") && !Stack[top].equals(")"))
                output.add(Stack[top]);
            pop();
        }
        clearStack();

        if (output.size() == 1) {
            String str = output.get(0);
            Quadra tempQ = new Quadra(-1);
            if (str.equals("true") || str.equals("false"))
                tempQ.value = Boolean.parseBoolean(str);
            else if (isNumber(str)) {
                if (varType == Kind.Integer)
                    tempQ.value = Integer.parseInt(str);
                else tempQ.value = Double.parseDouble(str);
            } else {
                tempQ.index = findVariable(str, codeLevel);
            }
            String TempName = "*Temp" + TempCount;
            TempCount++;
            SemanticSymbol nSymbol = new SemanticSymbol(TempName, varType, codeLevel, line, column);
            SymbolTable.add(nSymbol);
            if (tempQ.index < 0) {
                QuadronList.add(new Quadron("empty", null, null, 0));
                nSymbol.value = tempQ.value;
            } else {
                QuadronList.add(new Quadron("give", tempQ, null, SymbolTable.size() - 1));
            }
        }
        else {
            //生成四元式
            for (String str : output) {
                if (isOperator(str) > 0) {
                    String arg1 = SymbolStack[STop], arg2 = SymbolStack[STop - 1];
                    SPop();
                    SPop();
                    Quadra q1 = new Quadra(-1), q2 = new Quadra(-1);
                    if (arg1.equals("true"))
                        q1.value = true;
                    else if (isNumber(arg1)) {
                        if (varType == Kind.Integer)
                            q1.value = Integer.parseInt(arg1);
                        else q1.value = Double.parseDouble(arg1);
                    } else {
                        q1.index = findVariable(arg1, codeLevel);
                    }
                    if (arg2.equals("false"))
                        q2.value = false;
                    else if (isNumber(arg2)) {
                        if (varType == Kind.Integer)
                            q2.value = Integer.parseInt(arg2);
                        else q2.value = Double.parseDouble(arg2);
                    } else {
                        q2.index = findVariable(arg2, codeLevel);
                    }
                    String TempName = "*Temp" + TempCount;
                    SymbolTable.add(new SemanticSymbol(TempName, Kind.Real, codeLevel, line, column));
                    TempCount++;
                    Quadron quadron = new Quadron(str, q2, q1, SymbolTable.size() - 1);
                    QuadronList.add(quadron);
                    SPush(TempName);
                } else {
                    SPush(str);
                }
            }
        }
    }

    private void InterpretBodyStm(TreeNode body_stm, int conditionIndex, int codeLevel, int loop, TreeNode assign_expr) {

        if (conditionIndex > 0) {
            Quadron jumpQuadr = new Quadron("jmp", new Quadra(conditionIndex), null, -1);
            QuadronList.add(jumpQuadr);
            TreeNode sub_program = null;
            if (body_stm.Children[0].symbol == TerminalS.LEFT_BRACE)
                sub_program = body_stm.Children[1];
            else sub_program = body_stm.Children[0];
            codeLevel++;
            //展开sub_program
            dfs(sub_program, codeLevel);
            if (assign_expr != null) {
                String assign_varID = assign_expr.Children[0].name;
                int assign_left_index = findVariable(assign_varID, codeLevel);
                Kind assign_varType = SymbolTable.get(assign_left_index).type;
                TreeNode assign_exp = assign_expr.Children[1].Children[1];
                int line = assign_expr.Children[0].line, column = assign_expr.Children[0].column;
                InterpretExpStm(assign_exp, assign_varType, codeLevel, line, column);

                Quadron quadron = new Quadron("=", new Quadra(SymbolTable.size() - 1), null, assign_left_index);
                QuadronList.add(quadron);
            }
            if (loop >= 0) {
                Quadra tQuadra = new Quadra(-1);
                tQuadra.value = false;
                Quadron jumpBack = new Quadron("jmp", tQuadra, null, loop);
                QuadronList.add(jumpBack);
            }
            jumpQuadr.setJump(QuadronList.size());//回填
        } else {
            TreeNode sub_program = null;
            if (body_stm.Children[0].symbol == TerminalS.LEFT_BRACE)
                sub_program = body_stm.Children[1];
            else sub_program = body_stm.Children[0];
            codeLevel++;
            //展开sub_program
            dfs(sub_program, codeLevel);
        }
        codeLevel--;

    }

    public void dfs(TreeNode node, int codeLevel) {
        Symbol symbol = node.symbol;
        if (!symbol.isTerminal()) {
            boolean expand = true;
            NTerminalS nT = (NTerminalS) symbol;

            //声明语句翻译
            if (nT == NTerminalS.declare_stm) {
                expand = false;
                String varID = node.Children[1].name;
                int Line = node.Children[1].line, Column = node.Children[1].column;
                Kind varType = null;
                String S_Type = node.Children[0].name;
                if (S_Type.equals("int"))
                    varType = Kind.Integer;
                else if (S_Type.equals("double") || S_Type.equals("float"))
                    varType = Kind.Real;
                else if (S_Type.equals("bool"))
                    varType = Kind.Boolean;
                int left_index = findVariable(varID, codeLevel);
                TreeNode post_stm = node.Children[2];
                if ((post_stm.Children.length > 1) && (post_stm.Children[1].symbol == NTerminalS.expression)) {
                    //ex: int a = 1
                    TreeNode numeric_expression = post_stm.Children[1];
                    InterpretExpStm(numeric_expression, varType, codeLevel, Line, Column);
                    //赋值
                    Quadron quadron = new Quadron("=", new Quadra(SymbolTable.size() - 1), null, left_index);
                    QuadronList.add(quadron);
                }
            }

            //赋值语句
            else if (nT == NTerminalS.assign_stm) {
                expand = false;
                String varID = node.Children[0].name;
                int Line = node.Children[0].line, Column = node.Children[0].column;
                int left_index = findVariable(varID, codeLevel);
                Kind varType = SymbolTable.get(left_index).type;
                TreeNode assign_post_stm = node.Children[1];
                if (assign_post_stm.Children[0].symbol == TerminalS.ASSIGN) {
                    //a = 1
                    TreeNode numeric_expression = node.Children[1].Children[1];
                    InterpretExpStm(numeric_expression, varType, codeLevel, Line, Column);
                    //赋值
                    Quadron quadron = new Quadron("=", new Quadra(SymbolTable.size() - 1), null, left_index);
                    QuadronList.add(quadron);
                } else {
                    //a[i] = 1
                    //计算下标
                    TreeNode index_expr = assign_post_stm.Children[1], value_expr = assign_post_stm.Children[4];
                    InterpretExpStm(index_expr, Kind.Integer, codeLevel, Line, -1);
                    Quadra q1 = new Quadra(left_index), q2 = new Quadra(SymbolTable.size() - 1);
                    InterpretExpStm(value_expr, varType, codeLevel, Line, -1);
                    Quadron quadron = new Quadron("=", q1, q2, SymbolTable.size() - 1);
                    //赋值
                    QuadronList.add(quadron);
                }
            }

            //if判断语句
            else if (nT == NTerminalS.if_stm) {
                expand = false;
                TreeNode condition_expr = node.Children[2].Children[0], body_stm = node.Children[4];
                TreeNode post_stm = node.Children[5];
                int Line = node.Children[0].line, Column = node.Children[0].column;
                //计算Condition
                InterpretExpStm(condition_expr, Kind.Boolean, codeLevel, Line, Column);
                InterpretBodyStm(body_stm, SymbolTable.size() - 1, codeLevel, -1, null);
                //判断Else
                if (post_stm.Children[0].symbol == NTerminalS.else_stm) {
                    //TreeNode
                    TreeNode body_else = post_stm.Children[0].Children[1];
                    InterpretBodyStm(body_else, -1, codeLevel, -1, null);
                }
            }

            //while语句
            else if (nT == NTerminalS.while_stm) {
                expand = false;
                TreeNode condition_expr = node.Children[2].Children[0], body_stm = node.Children[4];
                int Line = node.Children[0].line, Column = node.Children[1].column;
                InterpretExpStm(condition_expr, Kind.Boolean, codeLevel, Line, Column);
                InterpretBodyStm(body_stm, SymbolTable.size() - 1, codeLevel, QuadronList.size() - 1, null);
            }

            //for循环
            else if (nT == NTerminalS.for_stm) {
                expand = false;
                //初始化条件
                TreeNode init_stm = node.Children[2];
                String varID = init_stm.Children[0].name;
                int left_index = findVariable(varID, codeLevel);
                int Line = init_stm.Children[0].line, Column = init_stm.Children[0].column;
                Kind varType = SymbolTable.get(left_index).type;
                TreeNode init_expr = init_stm.Children[2];
                InterpretExpStm(init_expr, varType, codeLevel, Line, Column);
                //赋值
                Quadron quadron = new Quadron("=", new Quadra(SymbolTable.size() - 1), null, left_index);
                QuadronList.add(quadron);

                //循环增量语句
                TreeNode assign_expr = node.Children[6];

                //循环条件判断
                TreeNode condition_expr = node.Children[4].Children[0];
                TreeNode body_stm = node.Children[8];
                InterpretExpStm(condition_expr, Kind.Boolean, codeLevel, Line, Column);
                InterpretBodyStm(body_stm, SymbolTable.size() - 1, codeLevel, QuadronList.size() - 1, assign_expr);
            }

            if (expand && node.Children != null)
                for (TreeNode children : node.Children)
                    dfs(children, codeLevel);
        }

    }
}
