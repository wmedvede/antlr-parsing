package parser.descr;

public class ParameterDescr extends AnnotationsContainerDescr implements HasType {

    private String name;

    private TypeDescr type;

    public ParameterDescr(ElementType elementType) {
        super(elementType);
    }

    public ParameterDescr(ElementType elementType, String text, int start, int line, int position, String name) {
        this(elementType, text, start, -1, line, position, name);
    }

    public ParameterDescr(ElementType elementType, String text, int start, int stop, String name) {
        this(elementType, text, start, stop, -1, -1, name);
    }

    public ParameterDescr(ElementType elementType, String text, int start, int stop, int line, int position, String name) {
        super(elementType, text, start, stop, line, position);
        this.name = name;
    }

    @Override
    public ParameterDescr setType(TypeDescr type) {
        this.type = type;
        return this;
    }

    @Override
    public TypeDescr getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
