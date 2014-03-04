package parser.metadata;


public class EllipsisParameterDeclarationDesc extends ParameterDeclarationDesc {

    TextTokenElementDescriptor ellipsisToken;

    public EllipsisParameterDeclarationDesc() {
        super(ElementType.ELLIPSIS_PARAMETER);
    }

    public EllipsisParameterDeclarationDesc(String text, int start, int line, int position, String name) {
        this(text, start, -1, line, position, name);
    }

    public EllipsisParameterDeclarationDesc(String text, int start, int stop, String name) {
        this(text, start, stop, -1, -1, name);
    }

    public EllipsisParameterDeclarationDesc(String text, int start, int stop, int line, int position, String name) {
        super(ElementType.ELLIPSIS_PARAMETER, text, start, stop, line, position, name);
    }

    public TextTokenElementDescriptor getEllipsisToken() {
        return ellipsisToken;
    }

    public void setEllipsisToken(TextTokenElementDescriptor ellipsisToken) {
        this.ellipsisToken = ellipsisToken;
    }
}
