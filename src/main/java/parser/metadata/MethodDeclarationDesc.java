package parser.metadata;

public class MethodDeclarationDesc extends AnnotationsContainerDesc implements HasDimensions {

    private String name;

    private int dimensions = 0;

    //private List parameters

    //private List exceptions

    public MethodDeclarationDesc() {
        super(ElementType.METHOD);
    }

    public MethodDeclarationDesc(String text, int start, int line, int position) {
        this(text, start, -1, line, position);
    }

    public MethodDeclarationDesc(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public MethodDeclarationDesc(String text, int start, int stop, int line, int position) {
        super(ElementType.METHOD, text, start, stop, line, position);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDimensions() {
        return dimensions;
    }

    public void addDimension() {
        dimensions++;
    }
}
