package parser.metadata;


import java.util.ArrayList;
import java.util.List;

public class FieldDescr extends AnnotationsContainerDescr implements HasType {

    private List<VariableDeclarationDescr> variableDeclarations = new ArrayList<VariableDeclarationDescr>();

    private TypeDescr type;

    public FieldDescr() {
        super(ElementType.FIELD);
    }

    public FieldDescr(String text, int start, int end) {
        this(text, start, end, -1, -1);
    }

    public FieldDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.FIELD, text, start, stop, line, position);
    }

    public TypeDescr getType() {
        return type;
    }

    public void setType(TypeDescr type) {
        this.type = type;
    }

    public List<VariableDeclarationDescr> getVariableDeclarations() {
        return variableDeclarations;
    }

    public void setVariableDeclarations(List<VariableDeclarationDescr> variableDeclarations) {
        this.variableDeclarations = variableDeclarations;
    }

    public void addVariableDeclaration(VariableDeclarationDescr variableDeclarationDescr) {
        variableDeclarations.add(variableDeclarationDescr);
    }
}
