package parser.descr;


public class TypeArgumentListDescr extends ElementDescriptor {

    public TypeArgumentListDescr() {
        super(ElementType.TYPE_ARGUMENT_LIST);
    }

    public TypeArgumentListDescr(String text, int start, int line, int position) {
        this(text, start, -1, line, position);
    }

    public TypeArgumentListDescr(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public TypeArgumentListDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.TYPE_ARGUMENT_LIST, text, start, stop, line, position);
    }

    public JavaTokenDescr getLTStart() {
        return (JavaTokenDescr)getElements2().getFirst(ElementType.JAVA_LT);
    }

    public TypeArgumentListDescr setLTStart(JavaTokenDescr start) {
        getElements2().removeFirst(ElementType.JAVA_LT);
        getElements2().add(0, start);
        return this;
    }

    public JavaTokenDescr getGTStop() {
        return (JavaTokenDescr)getElements2().getFirst(ElementType.JAVA_GT);
    }

    public TypeArgumentListDescr setGTStop(JavaTokenDescr stop) {
        getElements2().removeFirst(ElementType.JAVA_GT);
        getElements2().add(stop);
        return this;
    }

    public void addArgument(TypeArgumentDescr argument) {
        getElements2().add(argument);
    }

}
