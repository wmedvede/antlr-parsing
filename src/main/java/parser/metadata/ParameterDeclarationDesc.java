package parser.metadata;

public class ParameterDeclarationDesc extends AnnotationsContainerDesc implements HasType {

    private String name;

    private TypeDesc type;

    public ParameterDeclarationDesc(ElementType elementType) {
        super(elementType);
    }

    public ParameterDeclarationDesc(ElementType elementType, String text, int start, int line, int position, String name) {
        this(elementType, text, start, -1, line, position, name);
    }

    public ParameterDeclarationDesc(ElementType elementType, String text, int start, int stop, String name) {
        this(elementType, text, start, stop, -1, -1, name);
    }

    public ParameterDeclarationDesc(ElementType elementType, String text, int start, int stop, int line, int position, String name) {
        super(elementType, text, start, stop, line, position);
        this.name = name;
    }

    @Override
    public void setType(TypeDesc type) {
        this.type = type;
    }

    @Override
    public TypeDesc getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
