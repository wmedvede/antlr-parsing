package parser.metadata;


public class VariableDeclarationDesc extends ElementDescriptor {

    private String identifier;

    /**
     * > 0 indicates that the variable is an array.
     * e.g. 1 -> int a[]
     * e.g. 2 -> int a[][]
     */
    private int dimensions = 0;

    /**
     * variableInitializer == null means that the variable wasn't initialized.
     */
    private VariableInitializerDesc variableInitializer;

    public VariableDeclarationDesc() {
        super(ElementType.VARIABLE);
    }

    public VariableDeclarationDesc(String text, int start, int stop, int line, int position,  String identifier) {
        super(ElementType.VARIABLE, text, start, stop, line, position);
        this.identifier = identifier;
    }

    public VariableDeclarationDesc(String text, int start, int stop, int line, int position) {
        this(text, start, stop, line, position, null);
    }

    public VariableDeclarationDesc(String text, int start, int stop, String identifier) {
        this(text, start, stop, -1, -1, identifier);
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getDimensions() {
        return dimensions;
    }

    public void setDimensions(int dimensions) {
        this.dimensions = dimensions;
    }

    public void addDimension() {
        dimensions++;
    }

    public VariableInitializerDesc getVariableInitializer() {
        return variableInitializer;
    }

    public void setVariableInitializer(VariableInitializerDesc variableInitializer) {
        this.variableInitializer = variableInitializer;
    }
}
