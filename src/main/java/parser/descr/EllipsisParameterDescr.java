package parser.descr;


public class EllipsisParameterDescr extends ParameterDescr {

    TextTokenElementDescr ellipsisToken;

    public EllipsisParameterDescr() {
        super(ElementType.ELLIPSIS_PARAMETER);
    }

    public EllipsisParameterDescr(String text, int start, int line, int position, String name) {
        this(text, start, -1, line, position, name);
    }

    public EllipsisParameterDescr(String text, int start, int stop, String name) {
        this(text, start, stop, -1, -1, name);
    }

    public EllipsisParameterDescr(String text, int start, int stop, int line, int position, String name) {
        super(ElementType.ELLIPSIS_PARAMETER, text, start, stop, line, position, name);
    }

    public TextTokenElementDescr getEllipsisToken() {
        return ellipsisToken;
    }

    public void setEllipsisToken(TextTokenElementDescr ellipsisToken) {
        this.ellipsisToken = ellipsisToken;
    }
}
