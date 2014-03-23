package parser.descr;

public class ParameterDescr extends ModifiersContainerDescr implements HasType {

    public ParameterDescr(ElementType elementType) {
        super(elementType);
    }

    public ParameterDescr(ElementType elementType, String text, int start, int line, int position) {
        this(elementType, text, start, -1, line, position);
    }

    public ParameterDescr(ElementType elementType, String text, int start, int stop) {
        this(elementType, text, start, stop, -1, -1);
    }

    public ParameterDescr(ElementType elementType, String text, int start, int stop, int line, int position) {
        super(elementType, text, start, stop, line, position);
    }

    public TypeDescr getType() {
        return (TypeDescr) getElements().getFirst(ElementType.TYPE);
    }

    public ParameterDescr setType(TypeDescr type) {
        getElements().removeFirst(ElementType.TYPE);
        getElements().add(type);
        return this;
    }

    public IdentifierDescr getIdentifier() {
        return (IdentifierDescr) getElements().getFirst(ElementType.IDENTIFIER);
    }

    public ParameterDescr setIdentifier(IdentifierDescr identifier) {
        getElements().removeFirst(ElementType.IDENTIFIER);
        getElements().add(identifier);
        return this;
    }

    public JavaTokenDescr getStartComma() {
        return (JavaTokenDescr) getElements().getFirst(ElementType.JAVA_COMMA);
    }

    public ParameterDescr setStartComma(JavaTokenDescr comma) {
        getElements().removeFirst(ElementType.JAVA_COMMA);
        getElements().add(0, comma);
        return this;
    }

}
