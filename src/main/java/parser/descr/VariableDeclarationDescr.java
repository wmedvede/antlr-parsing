package parser.descr;


import java.util.ArrayList;
import java.util.List;

public class VariableDeclarationDescr extends ElementDescriptor implements HasDimensions {

    private String identifier;

    /**
     * > 0 indicates that the variable is an array.
     * e.g. 1 -> int a[]
     * e.g. 2 -> int a[][]
     */
    private List<DimensionDescr> dimensions = new ArrayList<DimensionDescr>();

    /**
     * variableInitializer == null means that the variable wasn't initialized.
     */
    private VariableInitializerDescr variableInitializer;

    public VariableDeclarationDescr() {
        super(ElementType.VARIABLE);
    }

    public VariableDeclarationDescr(String text, int start, int stop, int line, int position, String identifier, VariableInitializerDescr variableInitializer) {
        super(ElementType.VARIABLE, text, start, stop, line, position);
        this.identifier = identifier;
        this.variableInitializer = variableInitializer;
    }

    public VariableDeclarationDescr(String text, int start, int stop, int line, int position) {
        this(text, start, stop, line, position, null, null);
    }

    public VariableDeclarationDescr(String text, int start, int stop, String identifier) {
        this(text, start, stop, -1, -1, identifier, null);
    }

    public VariableDeclarationDescr(String text, int start, int stop, String identifier, VariableInitializerDescr variableInitializer) {
        this(text, start, stop, -1, -1, identifier, variableInitializer);
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setIdentifierDescr(IdentifierDescr identifier) {
        getElements2().removeFirst(ElementType.IDENTIFIER);
        getElements2().add(identifier);
    }

    public IdentifierDescr getIdentifierDescr() {
        return (IdentifierDescr)getElements2().getFirst(ElementType.IDENTIFIER);
    }

    @Override
    public int getDimensionsCount() {
        return dimensions.size();
    }

    @Override
    public void addDimension(DimensionDescr dimensionDescr) {
        dimensions.add(dimensionDescr);
    }

    public VariableInitializerDescr getVariableInitializer() {
        return (VariableInitializerDescr)getElements2().getFirst(ElementType.VARIABLE_INITIALIZER);
        //return variableInitializer;
    }

    public void setVariableInitializer(VariableInitializerDescr variableInitializer) {
        getElements2().add(variableInitializer);
        //this.variableInitializer = variableInitializer;
    }

}