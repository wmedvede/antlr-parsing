package parser.descr;

import java.util.ArrayList;
import java.util.List;

public class IdentifierWithTypeArgumentsDescr extends ElementDescriptor implements HasTypeArguments {

    private String name;

    private List<TypeArgumentDescr> arguments = new ArrayList<TypeArgumentDescr>();

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
    public List<TypeArgumentDescr> getArguments() {
        return arguments;
    }

    @Override
    public void addArgument(TypeArgumentDescr typeArgument) {
        arguments.add(typeArgument);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        getElements2().add(0, identifier);
        return this;
    }



}
