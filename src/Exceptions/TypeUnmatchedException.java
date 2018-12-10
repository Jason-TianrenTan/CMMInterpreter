package Exceptions;


import static SemanticAnalysis.SemanticSymbol.Kind;

public class TypeUnmatchedException extends CMMException {


    private Kind requiredType, foundType;
    public TypeUnmatchedException(int l, int c, Kind required, Kind found) {
        super(l, c);
        requiredType = required;
        foundType = found;
    }

    @Override
    public String toString() {
        return super.toString() + "Required type " + requiredType.name() + ", found " + foundType.name();
    }

}
