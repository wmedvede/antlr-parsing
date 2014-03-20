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
        return (TypeDescr)getElements2().getFirst(ElementType.TYPE);
    }

    public TypeArgumentDescr setType(TypeDescr type) {
        getElements2().removeFirst(ElementType.TYPE);
        getElements2().add(type);
        return this;
    }

    public TypeArgumentDescr setStartComma(JavaTokenDescr comma) {
        getElements2().removeFirst(ElementType.JAVA_COMMA);
        getElements2().add(0, comma);
        return this;
    }
}
