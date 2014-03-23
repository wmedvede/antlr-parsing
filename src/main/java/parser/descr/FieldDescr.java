package parser.descr;


import java.util.ArrayList;
import java.util.List;

public class FieldDescr extends ModifiersContainerDescr implements HasType {

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
        return (TypeDescr) getElements().getFirst(ElementType.TYPE);
    }

    public FieldDescr setType(TypeDescr type) {
        getElements().removeFirst(ElementType.TYPE);
        getElements().add(type);
        return this;
    }

    public List<VariableDeclarationDescr> getVariableDeclarations() {
        List<VariableDeclarationDescr> variableDeclarations = new ArrayList<VariableDeclarationDescr>();
        for (ElementDescriptor member :  getElements().getElementsByType(ElementType.VARIABLE)) {
            variableDeclarations.add((VariableDeclarationDescr)member);
        }
        return variableDeclarations;
    }

    public FieldDescr addVariableDeclaration(VariableDeclarationDescr variableDeclarationDescr) {
        getElements().add(variableDeclarationDescr);
        return this;
    }

    public boolean removeVariableDeclaration(VariableDeclarationDescr variableDeclarationDescr) {
        return getElements().remove(variableDeclarationDescr);
    }

    public JavaTokenDescr getEndSemiColon() {
        return (JavaTokenDescr) getElements().getLast(ElementType.JAVA_SEMI_COLON);
    }

    public FieldDescr setEndSemiColon(JavaTokenDescr element) {
        getElements().removeFirst(ElementType.JAVA_SEMI_COLON);
        getElements().add(element);
        return this;
    }

    public VariableDeclarationDescr getVariableDeclaration(String name) {
        if (name == null) return null;
        IdentifierDescr identifier;
        for (VariableDeclarationDescr variable : getVariableDeclarations()) {
            identifier = variable.getIdentifier();
            if (identifier != null && name.equals(identifier.getIdentifier())) {
                return variable;
            }
        }
        return null;
    }
}
