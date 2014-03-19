package parser.descr;

public class DimensionDescr extends ElementDescriptor {

    public DimensionDescr() {
        super(ElementType.DIMENSION);
    }

    public DimensionDescr(String text, int start, int line, int position) {
        this(text, start, -1, line, position);
    }

    public DimensionDescr(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public DimensionDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.DIMENSION, text, start, stop, line, position);
    }

    public DimensionDescr(String text, int start, int stop, int line, int position, JavaTokenDescr startBracket, JavaTokenDescr endBracket) {
        super(ElementType.DIMENSION, text, start, stop, line, position);
        setStartBracket(startBracket);
        setEndBracket(endBracket);
    }

    public JavaTokenDescr getStartBracket() {
        return (JavaTokenDescr)getElements2().getElementsByType(ElementType.JAVA_LBRACKET);
    }

    public DimensionDescr setStartBracket(JavaTokenDescr startBracket) {
        getElements2().removeFirst(ElementType.JAVA_LBRACKET);
        getElements2().add(startBracket);
        return this;
    }

    public JavaTokenDescr getEndBracket() {
        return (JavaTokenDescr)getElements2().getElementsByType(ElementType.JAVA_RBRACKET);
    }

    public DimensionDescr setEndBracket(JavaTokenDescr endBracket) {
        getElements2().removeFirst(ElementType.JAVA_RBRACKET);
        getElements2().add(endBracket);
        return this;
    }
}