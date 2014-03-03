package parser.metadata;


public class TypeArgumentDesc extends ElementDescriptor implements HasType {

    private TypeDesc type;

    public TypeArgumentDesc() {
        super(ElementType.TYPE_ARGUMENT);
    }

    public TypeArgumentDesc(String text, int start, int stop, int line, int position) {
        super(ElementType.TYPE_ARGUMENT, text, start, stop, line, position);
    }

    public TypeArgumentDesc(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public TypeDesc getType() {
        return type;
    }

    public void setType(TypeDesc type) {
        this.type = type;
    }
}
