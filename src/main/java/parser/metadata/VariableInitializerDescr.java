package parser.metadata;


public class VariableInitializerDescr extends ElementDescriptor {

    private String initializerExpr;

    public VariableInitializerDescr() {
        super(ElementType.VARIABLE_INITIALIZER);
    }

    public VariableInitializerDescr(String text, int start, int stop, int line, int position, String initializerExpr) {
        super(ElementType.VARIABLE_INITIALIZER, text, start, stop, line, position);
        this.initializerExpr = initializerExpr;
    }

    public VariableInitializerDescr(String text, int start, int stop, String initializerExpr) {
        this(text, start, stop, -1, -1, initializerExpr);
    }

    public String getInitializerExpr() {
        return initializerExpr;
    }

    public void setInitializerExpr(String initializerExpr) {
        this.initializerExpr = initializerExpr;
    }
}
