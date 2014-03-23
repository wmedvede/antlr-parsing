package parser.descr;


public class TypeArgumentDescr extends ElementDescriptor implements HasType {

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
        return (TypeDescr) getElements().getFirst(ElementType.TYPE);
    }

    public TypeArgumentDescr setType(TypeDescr type) {
        getElements().removeFirst(ElementType.TYPE);
        getElements().add(type);
        return this;
    }

    public TypeArgumentDescr setStartComma(JavaTokenDescr comma) {
        getElements().removeFirst(ElementType.JAVA_COMMA);
        getElements().add(0, comma);
        return this;
    }
}
