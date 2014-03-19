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
        return (TypeDescr)getElements2().getFirst(ElementType.TYPE);
    }

    public FieldDescr setType(TypeDescr type) {
        getElements2().removeFirst(ElementType.TYPE);
        getElements2().add(type);
        return this;
    }

    public List<VariableDeclarationDescr> getVariableDeclarations() {
        List<VariableDeclarationDescr> variableDeclarations = new ArrayList<VariableDeclarationDescr>();
        for (ElementDescriptor member :  getElements2().getElementsByType(ElementType.VARIABLE)) {
            variableDeclarations.add((VariableDeclarationDescr)member);
        }
        return variableDeclarations;
    }

    public FieldDescr addVariableDeclaration(VariableDeclarationDescr variableDeclarationDescr) {
        getElements2().add(variableDeclarationDescr);
        return this;
    }

    public FieldDescr removeVariableDeclaration(VariableDeclarationDescr variableDeclarationDescr) {
        getElements2().remove(variableDeclarationDescr);
        return this;
    }

    public JavaTokenDescr getEndSemiColon() {
        return (JavaTokenDescr)getElements2().getLast(ElementType.JAVA_SEMI_COLON);
    }

    public FieldDescr setEndSemiColon(JavaTokenDescr element) {
        getElements2().removeFirst(ElementType.JAVA_SEMI_COLON);
        getElements2().add(element);
        return this;
    }
}
