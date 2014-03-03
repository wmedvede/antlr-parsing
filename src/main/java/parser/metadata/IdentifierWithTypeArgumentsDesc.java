package parser.metadata;

import java.util.ArrayList;
import java.util.List;

public class IdentifierWithTypeArgumentsDesc extends ElementDescriptor implements HasTypeArguments {

    private String name;

    private List<TypeArgumentDesc> arguments = new ArrayList<TypeArgumentDesc>();

    public IdentifierWithTypeArgumentsDesc() {
        super(ElementType.IDENTIFIER_WITH_TYPE_ARGUMENTS);
    }

    public IdentifierWithTypeArgumentsDesc(String text, int start, int line, int position, String name) {
        this(text, start, -1, line, position, name);
    }

    public IdentifierWithTypeArgumentsDesc(String text, int start, int stop) {
        this(text, start, stop, -1, -1, null);
    }

    public IdentifierWithTypeArgumentsDesc(String text, int start, int stop, int line, int position, String name) {
        super(ElementType.IDENTIFIER_WITH_TYPE_ARGUMENTS, text, start, stop, line, position);
        this.name = name;
    }

    @Override
    public List<TypeArgumentDesc> getArguments() {
        return arguments;
    }

    @Override
    public void addArgument(TypeArgumentDesc typeArgument) {
        arguments.add(typeArgument);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
