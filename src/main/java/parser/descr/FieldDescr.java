package parser.descr;


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
        return (TypeDescr)getElements2().getElementsByType(ElementType.TYPE);
    }

    public void setType(TypeDescr type) {
        getElements2().add(type);
    }

    public List<VariableDeclarationDescr> getVariableDeclarations() {

        List<VariableDeclarationDescr> variableDeclarations = new ArrayList<VariableDeclarationDescr>();
        for (ElementDescriptor member :  getElements2().getElementsByType(ElementType.VARIABLE)) {
            variableDeclarations.add((VariableDeclarationDescr)member);
        }
        return variableDeclarations;
    }

    public void setVariableDeclarations(List<VariableDeclarationDescr> variableDeclarations) {
        this.variableDeclarations = variableDeclarations;
    }

    public void addVariableDeclaration(VariableDeclarationDescr variableDeclarationDescr) {
        getElements2().add(variableDeclarationDescr);
        //variableDeclarations.add(variableDeclarationDescr);
    }

    public void setVariableDeclarationsStop(ElementDescriptor stop) {
        getElements2().add(stop);
    }

}
