package parser.metadata;


import java.util.ArrayList;
import java.util.List;

public class VariableDeclarationDesc extends ElementDescriptor implements HasDimensions {

    private String identifier;

    /**
     * > 0 indicates that the variable is an array.
     * e.g. 1 -> int a[]
     * e.g. 2 -> int a[][]
     */
    private List<DimensionDesc> dimensions = new ArrayList<DimensionDesc>();

    /**
     * variableInitializer == null means that the variable wasn't initialized.
     */
    private VariableInitializerDesc variableInitializer;

    public VariableDeclarationDesc() {
        super(ElementType.VARIABLE);
    }

    public VariableDeclarationDesc(String text, int start, int stop, int line, int position,  String identifier, VariableInitializerDesc variableInitializer) {
        super(ElementType.VARIABLE, text, start, stop, line, position);
        this.identifier = identifier;
        this.variableInitializer = variableInitializer;
    }

    public VariableDeclarationDesc(String text, int start, int stop, int line, int position) {
        this(text, start, stop, line, position, null, null);
    }

    public VariableDeclarationDesc(String text, int start, int stop, String identifier) {
        this(text, start, stop, -1, -1, identifier, null);
    }

    public VariableDeclarationDesc(String text, int start, int stop, String identifier, VariableInitializerDesc variableInitializer) {
        this(text, start, stop, -1, -1, identifier, variableInitializer);
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public int getDimensionsCount() {
        return dimensions.size();
    }

    @Override
    public void addDimension(DimensionDesc dimensionDesc) {
        dimensions.add(dimensionDesc);
    }

    public VariableInitializerDesc getVariableInitializer() {
        return variableInitializer;
    }

    public void setVariableInitializer(VariableInitializerDesc variableInitializer) {
        this.variableInitializer = variableInitializer;
    }
}