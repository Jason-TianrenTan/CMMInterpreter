package Analysis;

/**
 * 产生式
 */
public class Rule {
   public NTerminalS leftParam;
   public Symbol[] rightParams;
   public Rule(NTerminalS left, Symbol[] right) {
       leftParam = left;
       rightParams = right;
   }

   @Override
   public String toString() {
       StringBuilder sb = new StringBuilder();
       sb.append(leftParam);
       sb.append("->");
       for (Symbol symbol : rightParams)
           sb.append("<" + symbol + ">");
       return sb.toString();
   }

}
