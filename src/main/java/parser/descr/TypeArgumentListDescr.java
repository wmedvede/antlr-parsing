package parser.descr;


public class TypeArgumentListDescr extends ElementDescriptor {

    public TypeArgumentListDescr(ElementType elementType) {
        super(elementType);
    }

    public TypeArgumentListDescr(ElementType elementType, String text, int start, int line, int position) {
        super(elementType, text, start, line, position);
    }

    public TypeArgumentListDescr(ElementType elementType, String text, int start, int stop) {
        super(elementType, text, start, stop);
    }

    public TypeArgumentListDescr(ElementType elementType, String text, int start, int stop, int line, int position) {
        super(elementType, text, start, stop, line, position);
    }
}
