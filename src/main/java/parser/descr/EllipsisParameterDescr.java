package parser.descr;


public class EllipsisParameterDescr extends ParameterDescr {

    public EllipsisParameterDescr() {
        super(ElementType.ELLIPSIS_PARAMETER);
    }

    public EllipsisParameterDescr(String text, int start, int line, int position) {
        this(text, start, -1, line, position);
    }

    public EllipsisParameterDescr(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public EllipsisParameterDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.ELLIPSIS_PARAMETER, text, start, stop, line, position);
    }

    public JavaTokenDescr getEllipsisToken() {
        return (JavaTokenDescr) getElements().getFirst(ElementType.JAVA_ELLIPSIS);
    }

    public void setEllipsisToken(JavaTokenDescr ellipsisToken) {
        getElements().removeFirst(ElementType.JAVA_ELLIPSIS);
        getElements().add(ellipsisToken);
    }
}
