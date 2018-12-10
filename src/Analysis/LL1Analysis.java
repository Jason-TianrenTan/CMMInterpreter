package Analysis;

import Exceptions.CMMException;
import Lexer.Token;
import SemanticAnalysis.SemanticAnalyzer;

import java.util.*;
import static Analysis.TerminalS.*;
import static Analysis.NTerminalS.*;

public class LL1Analysis {

    //产生式
    private ArrayList<Rule> rules;
    //FIRST FOLLOW SELECT
    private HashMap<Symbol, HashSet<TerminalS>> FIRST;
    private HashMap<NTerminalS, HashSet<TerminalS>> FOLLOW;
    private HashMap<Rule, HashSet<TerminalS>> SELECT;
    //预测分析表
    private HashMap<NTerminalS, HashMap<TerminalS, Symbol[]>> PredictionTable;
    //可推导出空
    private ArrayList<NTerminalS> CanEmptySet;
    //分析栈
    private Symbol[] Stack;
    private static int STACK_SIZE = 15000;
    private int top = -1;
    private int tree_top = -1;
    private TreeNode[] TreeStack;

    //报错
    private ArrayList<String> ErrorList;

    private int lines = 0;

    private boolean isCore(Symbol symbol) {
        Symbol[] Cores = {body_stm, program, sub_program, statement, condition};
        if (symbol.isTerminal())
            return false;
        for (Symbol s : Cores)
            if (symbol == s)
                return true;
        return false;
    }


    public void Analysis(ArrayList<Token> tokens) throws CMMException {

        Token tk = new Token(null, null, -1, -1);
        tk.symbol = hash;
        tokens.add(tk);

        push(hash);
        push(program);
        TreeNode Root = new TreeNode(program);
        //push(S);
        //TreeNode Root = new TreeNode(S);
        tree_push(new TreeNode(hash));
        tree_push(Root);
        for (Token token : tokens) {
            if (token.getLine() >= lines)
                lines = token.getLine();
        }
        while (tokens.size() > 0) {
            System.out.print("分析栈: ");
            for (int i=0;i<=top;i++)
                System.out.print(Stack[i]);
            System.out.println();
            System.out.print("剩余输入串: ");
            for (Token ts : tokens)
                System.out.print("<" + ts.symbol + ">");
            System.out.println();
            Symbol TopS = Stack[top];
            TreeNode TTop = TreeStack[tree_top];
            if ((TopS == hash) && (tokens.get(0).symbol == hash)) {
                System.out.println("接受");
                break;
            }
            if (TopS.isTerminal()) {
                if (TopS == tokens.get(0).symbol) {
                    //匹配
                    System.out.println("\"" + TopS + "\"匹配");
                    TTop.name = tokens.get(0).getContent();
                    TTop.line = tokens.get(0).getLine();
                    TTop.column = tokens.get(0).getColumn();
                    pop();
                    tree_pop();
                    tokens.remove(0);
                }
                else {
                    //出错处理，栈顶终结符不符合
                    int line = tokens.get(0).getLine(), column = tokens.get(0).getColumn();
                    ErrorList.add("行" + line +
                            "列" + column + ": 缺少符号\"" + TopS + "\"");
                    int err_count = 0;
                    while (!isCore(Stack[top])) {
                        TreeStack[tree_top].isError = true;
                        pop();
                        tree_pop();
                    }
                }
            }
            else {
                System.out.println("TopS = " + TopS + ", symbol = " + tokens.get(0).symbol);
                if (PredictionTable.get((NTerminalS)TopS).containsKey(tokens.get(0).symbol)) {
                    //预测分析表匹配成功
                    Symbol[] symbols = PredictionTable.get((NTerminalS) TopS).get(tokens.get(0).symbol);
                    System.out.print("使用产生式 " + TopS + "->");
                    TreeNode[] nNodes = new TreeNode[symbols.length];

                    for (int i = 0; i < symbols.length; i++) {
                        System.out.print("<" + symbols[i] + ">");
                        nNodes[i] = new TreeNode(symbols[i]);
                    }
                    System.out.println();
                    TTop.Children = nNodes;
                    //弹出非终结符
                    pop();
                    tree_pop();
                    for (int i = symbols.length - 1; i >= 0; i--) {
                        push(symbols[i]);
                        tree_push(nNodes[i]);
                    }

                }
                else {
                    //出错处理
                    int line = tokens.get(0).getLine(), column = tokens.get(0).getColumn();
                    if (tokens.get(0).symbol == hash) {
                        ErrorList.add("行" + lines + ": 文章意外终结");
                        break;
                    }
                    ErrorList.add("行" + line +
                            "列" + column + ": 未期待的符号 \"" + tokens.get(0).symbol + "\"");
                    System.out.println("行" + line +
                            "列" + column + ": 未期待的符号 \"" + tokens.get(0).symbol + "\"");
                    if ((Stack[top] == program) || (Stack[top] == sub_program)) {
                        tokens.remove(0);
                        while ((tokens.size() > 0) && !FIRST.get((NTerminalS)Stack[top]).contains(tokens.get(0).symbol)) {
                            if (tokens.get(0).getLine() >= 0) {
                                ErrorList.add("行" + tokens.get(0).getLine() +
                                        "列" + tokens.get(0).getColumn() + ": 未期待的符号 \"" + tokens.get(0).symbol + "\"");
                            }
                            tokens.remove(0);
                        }
                    }
                    else {
                        if (FOLLOW.containsKey(Stack[top])) {
                            System.out.println(tokens.get(0).symbol + ", "
                                    + FOLLOW.get(Stack[top]).contains(tokens.get(0).symbol));
                            while ((tokens.size() > 0) && !FOLLOW.get(Stack[top]).contains(tokens.get(0).symbol)
                                    && !FIRST.get(program).contains(tokens.get(0).symbol)
                                    && !FIRST.get(sub_program).contains(tokens.get(0).symbol)) {
                                tokens.remove(0);
                            }
                            TreeStack[tree_top].isError = true;
                            pop();
                            tree_pop();
                        }
                    }
                }
            }

        }

        TreeStateChanged = true;
        while (TreeStateChanged) {
            TreeStateChanged = false;
            check(Root);
        }
        dfs(Root, 0);
        System.out.println("\n\n\n\n报错信息:");
        for (String str : ErrorList)
            System.out.println(str);

        System.out.println();
        new SemanticAnalyzer(Root).Analysis();
    }

    private boolean TreeStateChanged = false;
    private void check(TreeNode node) {
        if (node.symbol == nil) {
            if (node.print) {
                node.print = false;
                TreeStateChanged = true;
            }
        }
        else {
            boolean flag = true;
            if (node.Children != null) {
                for (TreeNode children : node.Children) {
                    if (children.print) {
                        check(children);
                        flag = false;
                    }
                }
                if (flag && node.print) {
                    node.print = false;
                    TreeStateChanged = true;
                }
            }

        }
    }

    private void dfs(TreeNode node, int level) {
        //if (!node.print)
        //    return;
        for (int i=0;i<level-1;i++)
            System.out.print(" ");
        if (level > 0)
            System.out.print("-");
        if (node.isError)
            System.out.println("ERROR");
        else {
            System.out.print(node.symbol);
            if (!node.symbol.isTerminal()) {
                System.out.println();
                if (node.Children != null)
                    for (TreeNode children : node.Children)
                        dfs(children, level + 1);
            }
            else {
                System.out.println("("+ node.name +")");
            }
        }
    }

    private void push(Symbol s) {
        if (s != nil)
            Stack[++top] = s;
    }

    private void pop() {
        top--;
    }

    private void tree_push(TreeNode node) {
        if (node.symbol != nil)
            TreeStack[++tree_top] = node;
    }

    private void tree_pop() {
        tree_top--;
    }
    public LL1Analysis() {
        rules = new ArrayList<>();
        FIRST = new HashMap<>();
        FOLLOW = new HashMap<>();
        SELECT = new HashMap<>();
        PredictionTable = new HashMap<>();
        Stack = new Symbol[STACK_SIZE];
        TreeStack = new TreeNode[STACK_SIZE];
        //NTerminalS[] cEmpty = {SX, TX};
        NTerminalS[] cEmpty = {program, sub_program, if_post_stm,
                array_post_stm, exp_post_stm,
        declare_num_post_stm, declare_bool_post_stm, var_post_stm};
        CanEmptySet = new ArrayList<>(Arrays.asList(cEmpty));
        ErrorList = new ArrayList<>();
    }

    public void addRule(NTerminalS nT, Symbol... symbols) {
        rules.add(new Rule(nT, symbols));
    }

    boolean _modified = true;
    public void FindFollow(Rule rule) {
        //System.out.println("At rule : " + rule);
        NTerminalS left = rule.leftParam;
        Symbol[] symbols = rule.rightParams;
        for (int i=0;i<symbols.length;i++) {
            //逐个寻找非终结
            if (!symbols[i].isTerminal()) {
                if (i == (symbols.length - 1)) {
                    if (!FOLLOW.containsKey(symbols[i]))
                        FOLLOW.put((NTerminalS)symbols[i], new HashSet<TerminalS>());
                    //FOLLOW(A)加入FOLLOW(B)
                    if (FOLLOW.containsKey(left)) {
                        for (TerminalS ts : FOLLOW.get(left)) {
                            if (!FOLLOW.get((NTerminalS)symbols[i]).contains(ts)&& (ts != ELSE)) {
                                FOLLOW.get((NTerminalS) symbols[i]).add(ts);
                                _modified = true;
                            }
                        }
                    }
                }
                else {
                    Symbol nextS = symbols[i + 1];
                    if (!nextS.isTerminal()) {
                        //FIRST(b)中非空元素加入FOLLOW(B)
                        if (FIRST.containsKey(nextS)) {
                            if (!FOLLOW.containsKey(symbols[i]))
                                FOLLOW.put((NTerminalS)symbols[i], new HashSet<TerminalS>());

                            for (TerminalS ts : FIRST.get(nextS)) {
                                if (ts != nil) {
                                    if (!FOLLOW.get((NTerminalS)symbols[i]).contains(ts) && (ts != ELSE)) {
                                        _modified = true;
                                        FOLLOW.get((NTerminalS)symbols[i]).add(ts);
                                    }
                                }
                            }

                            if (FIRST.get(nextS).contains(nil)) {
                                //可以推出空
                                //FOLLOW(A)加入到FOLLOW(B)
                                if (FOLLOW.containsKey(left)) {
                                    for (TerminalS ts : FOLLOW.get(left)) {
                                        if (!FOLLOW.get((NTerminalS) symbols[i]).contains(ts)&& (ts != ELSE)) {
                                            FOLLOW.get((NTerminalS) symbols[i]).add(ts);
                                            _modified = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else {
                        if (!FOLLOW.containsKey(symbols[i]))
                            FOLLOW.put((NTerminalS)symbols[i], new HashSet<TerminalS>());
                        if (!FOLLOW.get((NTerminalS)symbols[i]).contains(nextS)&& (nextS != ELSE)) {
                            FOLLOW.get((NTerminalS) symbols[i]).add((TerminalS)nextS);
                            _modified = true;
                        }
                    }
                }
            }
        }
    }

    boolean canEmpty = false;
    public void search(Rule rule, ArrayList<TerminalS> symbolQueue, int index, Symbol[] symbols) {
        if (index == rule.rightParams.length) {
            int pointer = 0;
            while ((pointer < symbolQueue.size()) && (symbolQueue.get(pointer) == nil))
                pointer++;
            if (pointer == symbolQueue.size()) {
                canEmpty = true;
                //a-*->e 可推导出空
                //求FIRST(a)-e与FOLLOW(a)的并集
            }
            else {
                SELECT.get(rule).add(symbolQueue.get(pointer));
            }
            return;
        }
        if (symbols[index].isTerminal()) {
            symbolQueue.add((TerminalS)symbols[index]);
            search(rule, symbolQueue, index + 1, symbols);
        }
        else {
            HashSet<TerminalS> nonTerminal = FIRST.get((NTerminalS) symbols[index]);
            for (TerminalS ts : nonTerminal) {
                ArrayList<TerminalS> nArray = new ArrayList<>();
                nArray = (ArrayList<TerminalS>) symbolQueue.clone();
                nArray.add(ts);
                search(rule, nArray, index + 1, symbols);
            }
        }
    }

    public void FindSelect(Rule rule) {
        if (!SELECT.containsKey(rule))
            SELECT.put(rule, new HashSet<TerminalS>());
        NTerminalS left = rule.leftParam;
        Symbol[] right = rule.rightParams;
        search(rule, new ArrayList<TerminalS>(), 0, right);

        if (canEmpty) {
            Iterator<TerminalS> iter = SELECT.get(rule).iterator();
            while (iter.hasNext()) {
                TerminalS ts = iter.next();
                if (ts == nil)
                    iter.remove();
            }
            HashSet<TerminalS> followA = FOLLOW.get(left);
            for (TerminalS ts : followA)
                SELECT.get(rule).add(ts);
        }
    }

    private int RuleCanEmpty(Rule rule) {
        Symbol[] symbols = rule.rightParams;
        int i=0;
        for (Symbol symbol : symbols) {
            if (!(!symbol.isTerminal() && CanEmptySet.contains((NTerminalS)symbol)))
                return i;
            i++;
        }
        return i;
    }

    private void CalculateFirstSet() {
        //计算FIRST
        for (TerminalS ts : TerminalS.values()) {
            FIRST.put(ts, new HashSet<>());
            FIRST.get(ts).add(ts);
        }
        for (NTerminalS nts : NTerminalS.values()) {
            FIRST.put(nts, new HashSet<>());
        }
        boolean modified = true;
        while (modified) {
            modified = false;
            for (Rule rule : rules) {
                NTerminalS left = rule.leftParam;
                Symbol[] symbols = rule.rightParams;
                //条件3，可直接推导出空
                if ((symbols.length == 1) && (symbols[0] == nil)) {
                    if (!FIRST.get(left).contains(nil)) {
                        FIRST.get(left).add(nil);
                        modified = true;
                    }
                }
                //条件2, X->a
                else {
                    if (symbols[0].isTerminal() && (symbols[0] != nil)) {
                        if (!FIRST.get(left).contains((TerminalS) symbols[0])) {
                            FIRST.get(left).add((TerminalS) symbols[0]);
                            modified = true;
                        }
                    }
                }
                //条件4 能否推出空
                int eCount = RuleCanEmpty(rule);
                if (eCount == symbols.length) {
                    for (int i = 0; i < symbols.length; i++) {
                        if (FIRST.containsKey(symbols[i])) {
                            HashSet<TerminalS> first = FIRST.get(symbols[i]);
                            for (TerminalS ts : first) {
                                if (!FIRST.get(left).contains(ts)) {
                                    FIRST.get(left).add(ts);
                                    modified = true;
                                }
                            }
                        }
                    }
                    FIRST.get(left).add(nil);
                } else {
                    for (int i = 0; i < eCount + 1; i++) {
                        if (FIRST.containsKey(symbols[i])) {
                            HashSet<TerminalS> first = FIRST.get(symbols[i]);
                            for (TerminalS ts : first) {
                                if (ts != nil) {
                                    if (!FIRST.get(left).contains(ts)) {
                                        FIRST.get(left).add(ts);
                                        modified = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public void CalculateSets() {
        CalculateFirstSet();
        //删除终结符的FIRST
        Iterator<Symbol> iter = FIRST.keySet().iterator();
        while (iter.hasNext()) {
            Symbol symbol = iter.next();
            if (symbol.isTerminal()) {
                iter.remove();
            }
        }

        for (Map.Entry<Symbol, HashSet<TerminalS>> entry : FIRST.entrySet()) {
            System.out.print("FIRST(" + entry.getKey() + ") : ");
            HashSet<TerminalS> tslist = entry.getValue();
            for (TerminalS ts : tslist)
                System.out.print("<" + ts + "> ");
            System.out.println();

        }
        System.out.println();

        FOLLOW.put(program, new HashSet<TerminalS>());
        FOLLOW.get(program).add(hash);
        //FOLLOW.put(S, new HashSet<TerminalS>());
        //FOLLOW.get(S).add(hash);
        while (_modified) {
            _modified = false;
            for (Rule rule : rules) {
                FindFollow(rule);
            }
        }




        for (Map.Entry<NTerminalS, HashSet<TerminalS>> entry : FOLLOW.entrySet()) {
            System.out.print("FOLLOW(" + entry.getKey() + ") : ");
            HashSet<TerminalS> tslist = entry.getValue();
            for (TerminalS ts : tslist)
                System.out.print("<" + ts + "> ");
            System.out.println();
        }
        System.out.println();

        for (Rule rule : rules) {
            canEmpty = false;
            FindSelect(rule);
        }



        for (Map.Entry<Rule, HashSet<TerminalS>> entry : SELECT.entrySet()) {
            Rule rule = entry.getKey();
            System.out.print("SELECT(" + rule.leftParam + "->");
            for (Symbol s : rule.rightParams)
                System.out.print(s);
            System.out.print(") : ");
            HashSet<TerminalS> tslist = entry.getValue();
            for (TerminalS ts : tslist)
                System.out.print("<" + ts + "> ");
            System.out.println();
        }

        for (Map.Entry<Rule, HashSet<TerminalS>> entry : SELECT.entrySet()) {
            Rule rule = entry.getKey();
            HashSet<TerminalS> terms = entry.getValue();
            NTerminalS nTKey = rule.leftParam;
            Symbol[] symbols = rule.rightParams;
            if (!PredictionTable.containsKey(nTKey)) {
                PredictionTable.put(nTKey, new HashMap<>());
            }
            for (TerminalS ts : terms)
                PredictionTable.get(nTKey).put(ts, symbols);
        }




        for (Map.Entry<NTerminalS, HashMap<TerminalS, Symbol[]>> entry : PredictionTable.entrySet()) {
            System.out.print(entry.getKey() + " : ");
            for (Map.Entry<TerminalS, Symbol[]> entry1 : entry.getValue().entrySet()) {
                System.out.print("->");
                for (Symbol sb : entry1.getValue())
                    System.out.print("<" + sb + ">");
                System.out.print("(" + entry1.getKey() + ")\t");
            }
            System.out.println();
        }

    }

}
