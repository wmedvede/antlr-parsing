package parser.metadata;


public class PrimitiveTypeDescr extends ElementDescriptor {

    private String name;

    public PrimitiveTypeDescr() {
        super(ElementType.PRIMITIVE_TYPE);
    }

    public PrimitiveTypeDescr(String text, int start, int stop, int line, int position, String name) {
        super(ElementType.PRIMITIVE_TYPE, text, start, stop, line, position);
        this.name = name;
    }

    public PrimitiveTypeDescr(String text, int start, int stop, String name) {
        this(text, start, stop, -1, -1, name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
