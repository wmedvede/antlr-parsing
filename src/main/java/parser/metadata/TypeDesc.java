package parser.metadata;


public class TypeDesc extends ElementDescriptor {

    private int dimensions = 0;

    private String name;

    public TypeDesc(ElementType elementType) {
        super(elementType);
    }

    public TypeDesc(ElementType elementType, String text, int start, int stop) {
        super(elementType, text, start, stop);
    }

    public TypeDesc(ElementType elementType, String text, int start, int stop, int line, int position, String name) {
        super(elementType, text, start, stop, line, position);
        this.name = name;
    }

    public int getDimensions() {
        return dimensions;
    }

    public void setDimensions(int dimensions) {
        this.dimensions = dimensions;
    }

    public void addDimension() {
        dimensions++;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
