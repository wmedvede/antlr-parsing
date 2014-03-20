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
        return (TypeDescr)getElements2().getFirst(ElementType.TYPE);
    }

    public ParameterDescr setType(TypeDescr type) {
        getElements2().removeFirst(ElementType.TYPE);
        getElements2().add(type);
        return this;
    }

    public IdentifierDescr getIdentifier() {
        return (IdentifierDescr)getElements2().getFirst(ElementType.IDENTIFIER);
    }

    public ParameterDescr setIdentifier(IdentifierDescr identifier) {
        getElements2().removeFirst(ElementType.IDENTIFIER);
        getElements2().add(identifier);
        return this;
    }

    public JavaTokenDescr getStartComma() {
        return (JavaTokenDescr)getElements2().getFirst(ElementType.JAVA_COMMA);
    }

    public ParameterDescr setStartComma(JavaTokenDescr comma) {
        getElements2().removeFirst(ElementType.JAVA_COMMA);
        getElements2().add(0, comma);
        return this;
    }

}
