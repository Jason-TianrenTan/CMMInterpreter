package SemanticAnalysis;

import Analysis.NTerminalS;
import Analysis.Symbol;
import Analysis.TerminalS;
import Analysis.TreeNode;
import Exceptions.CMMException;
import Exceptions.TypeUnmatchedException;
import Interpreter.InterCodeGenerator;

import java.lang.reflect.Array;
import java.util.*;
import static SemanticAnalysis.SemanticSymbol.Kind;

public class SemanticAnalyzer {

    private TreeNode root;
    private ArrayList<SemanticSymbol> symbolList;

    public SemanticAnalyzer(TreeNode r) {
        root = r;
        symbolList = new ArrayList<>();
    }

    public void Analysis() throws CMMException {
        dfs(root, 0);
        new InterCodeGenerator(symbolList, root).GenerateCode();
    }

    //判断符号表是否已经存在
    public int containsVariable(String varID) {
        for (SemanticSymbol ss : symbolList) {
            if (ss.name.equals(varID)) {
                return ss.level;
            }
        }
        return -1;
    }


    public void dfs(TreeNode node, int codeLevel) {
        Symbol symbol = node.symbol;
        if (!symbol.isTerminal()) {
            boolean inBody = false;
            NTerminalS nT = (NTerminalS)symbol;
            int Line = node.line, Column = node.column;
            if (nT == NTerminalS.body_stm) {
                codeLevel++;
                inBody = true;
            }

            if (nT == NTerminalS.declare_stm) {
                String varID = node.Children[1].name,
                        varType = node.Children[0].name;
                if (containsVariable(varID) < 0) {
                    //新符号
                    TreeNode post_stm = node.Children[2];
                    Kind type = null;
                    String name = varID;
                    if (post_stm.Children.length > 0 && (post_stm.Children[0].symbol == NTerminalS.declare_array_stm)) {
                        //数组类型
                        TreeNode declareArray = post_stm.Children[0];
                        int arraySize = Integer.parseInt(declareArray.Children[1].name);
                        Object val = null;
                        if (varType.equals("int")) {
                            val = new int[arraySize];
                            type = Kind.ArrayInt;
                        }
                        else if (varType.equals("float") || varType.equals("double")) {
                            val = new double[arraySize];
                            type = Kind.ArrayReal;
                        }
                        else if (varType.equals("bool")) {
                            val = new boolean[arraySize];
                            type = Kind.ArrayBool;
                        }
                        SemanticSymbol ArraySymbol = new SemanticSymbol(name, type, codeLevel, Line, Column);
                        ArraySymbol.type = type;
                        ArraySymbol.value = val;
                        ArraySymbol.size = arraySize;
                        symbolList.add(ArraySymbol);
                    }
                    else {
                        //普通类型
                        if (varType.equals("int"))
                            type = Kind.Integer;
                        else if (varType.equals("float") || varType.equals("double"))
                            type = Kind.Real;
                        else if (varType.equals("bool"))
                            type = Kind.Boolean;
                        symbolList.add(new SemanticSymbol(name, type, codeLevel, Line, Column));
                    }

                }
                else {
                    System.out.println("重复定义的标识符 \"" + varID + "\"");
                    System.exit(0);
                }
            }

            //调用语句
            else if ((nT == NTerminalS.assign_stm) || (nT == NTerminalS.variable) || (nT == NTerminalS.for_init_stm)) {
                int level = containsVariable(node.Children[0].name);
                if ((level < 0) || (level > codeLevel)) {
                    PrintUndefined(node.Children[0].name);
                }
            }

            //对于for,if,while,最外层循环条件视为同一层级，body再level++

            if (node.Children != null)
                for (TreeNode children : node.Children) {
                    dfs(children, codeLevel);
                }

            if (inBody)
                codeLevel--;
        }

    }

    private void PrintUndefined(String id) {
        System.out.println("未定义的标识符 \"" + id + "\"");
        System.exit(0);
    }
}
