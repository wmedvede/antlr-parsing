package parser.descr;


public class IdentifierWithTypeArgumentsDescr extends ElementDescriptor implements HasTypeArguments {

    public IdentifierWithTypeArgumentsDescr() {
        super(ElementType.IDENTIFIER_WITH_TYPE_ARGUMENTS);
    }

    public IdentifierWithTypeArgumentsDescr(String text, int start, int line, int position) {
        this(text, start, -1, line, position);
    }

    public IdentifierWithTypeArgumentsDescr(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public IdentifierWithTypeArgumentsDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.IDENTIFIER_WITH_TYPE_ARGUMENTS, text, start, stop, line, position);
    }

    @Override
    public TypeArgumentListDescr getArguments() {
        return (TypeArgumentListDescr)getElements2().getFirst(ElementType.TYPE_ARGUMENT_LIST);
    }

    public IdentifierWithTypeArgumentsDescr setArguments(TypeArgumentListDescr arguments) {
        getElements2().removeFirst(ElementType.TYPE_ARGUMENT_LIST);
        getElements2().add(arguments);
        return this;
    }

    public JavaTokenDescr getStartDot() {
        return (JavaTokenDescr)getElements2().getFirst(ElementType.JAVA_DOT);
    }

    public IdentifierWithTypeArgumentsDescr setStartDot(JavaTokenDescr dot) {
        getElements2().removeFirst(ElementType.JAVA_DOT);
        getElements2().add(0, dot);
        return this;
    }

    public IdentifierDescr getIdentifier() {
        return (IdentifierDescr)getElements2().getFirst(ElementType.IDENTIFIER);
    }

    public IdentifierWithTypeArgumentsDescr setIdentifier(IdentifierDescr identifier) {
        getElements2().removeFirst(ElementType.IDENTIFIER);
        getElements2().add(identifier);
        return this;
    }

}
