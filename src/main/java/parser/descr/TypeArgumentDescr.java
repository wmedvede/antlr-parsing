package parser.descr;


public class TypeArgumentDescr extends ElementDescriptor implements HasType {

    private TypeDescr type;

    public TypeArgumentDescr() {
        super(ElementType.TYPE_ARGUMENT);
    }

    public TypeArgumentDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.TYPE_ARGUMENT, text, start, stop, line, position);
    }

    public TypeArgumentDescr(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public TypeDescr getType() {
        return type;
    }

    public TypeArgumentDescr setType(TypeDescr type) {
        this.type = type;
        return this;
    }
}
