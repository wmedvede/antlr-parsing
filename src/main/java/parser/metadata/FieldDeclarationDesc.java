package parser.metadata;


import java.util.ArrayList;
import java.util.List;

public class FieldDeclarationDesc extends AnnotationsContainerDesc implements HasType {

    private List<VariableDeclarationDesc> variableDeclarations = new ArrayList<VariableDeclarationDesc>();

    private TypeDesc type;

    public FieldDeclarationDesc() {
        super(ElementType.FIELD);
    }

    public FieldDeclarationDesc(String text, int start, int end) {
        this(text, start, end, -1, -1);
    }

    public FieldDeclarationDesc(String text, int start, int stop, int line, int position) {
        super(ElementType.FIELD, text, start, stop, line, position);
    }

    public TypeDesc getType() {
        return type;
    }

    public void setType(TypeDesc type) {
        this.type = type;
    }

    public List<VariableDeclarationDesc> getVariableDeclarations() {
        return variableDeclarations;
    }

    public void setVariableDeclarations(List<VariableDeclarationDesc> variableDeclarations) {
        this.variableDeclarations = variableDeclarations;
    }

    public void addVariableDeclaration(VariableDeclarationDesc variableDeclarationDesc) {
        variableDeclarations.add(variableDeclarationDesc);
    }
}
