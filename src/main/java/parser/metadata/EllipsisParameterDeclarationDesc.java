package parser.metadata;


public class EllipsisParameterDeclarationDesc extends ParameterDeclarationDesc {

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
}
