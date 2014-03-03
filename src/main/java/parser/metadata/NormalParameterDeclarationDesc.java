package parser.metadata;

public class NormalParameterDeclarationDesc extends ParameterDeclarationDesc {

    private int dimensions = 0;

    public NormalParameterDeclarationDesc() {
        super(ElementType.NORMAL_PARAMETER);
    }

    public NormalParameterDeclarationDesc(String text, int start, int stop, String name) {
        this(text, start, stop, -1, -1, name);
    }

    public NormalParameterDeclarationDesc(String text, int start, int line, int position, String name) {
        this(text, start, -1, line, position, name);
    }

    public NormalParameterDeclarationDesc(String text, int start, int stop, int line, int position, String name) {
        super(ElementType.NORMAL_PARAMETER, text, start, stop, line, position, name);
    }

    public int getDimensions() {
        return dimensions;
    }

    public void addDimension() {
        dimensions++;
    }

}
