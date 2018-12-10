package Analysis;

import java.util.ArrayList;

public class TreeNode {
    public Symbol symbol;
    public TreeNode[] Children;
    public TreeNode parent;
    public String name;
    public int line, column;
    public TreeNode(Symbol s) {
        symbol = s;
    }
    public TreeNode(boolean error) { isError = true;}
    public boolean isError = false;
    public boolean print = true;
}
