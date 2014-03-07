package parser.descr;

import java.util.ArrayList;
import java.util.List;

public class IdentifierWithTypeArgumentsDescr extends ElementDescriptor implements HasTypeArguments {

    private String name;

    private List<TypeArgumentDescr> arguments = new ArrayList<TypeArgumentDescr>();

    public IdentifierWithTypeArgumentsDescr() {
        super(ElementType.IDENTIFIER_WITH_TYPE_ARGUMENTS);
    }

    public IdentifierWithTypeArgumentsDescr(String text, int start, int line, int position, String name) {
        this(text, start, -1, line, position, name);
    }

    public IdentifierWithTypeArgumentsDescr(String text, int start, int stop) {
        this(text, start, stop, -1, -1, null);
    }

    public IdentifierWithTypeArgumentsDescr(String text, int start, int stop, int line, int position, String name) {
        super(ElementType.IDENTIFIER_WITH_TYPE_ARGUMENTS, text, start, stop, line, position);
        this.name = name;
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
}
