package parser.metadata;


public class TypeDesc extends ElementDescriptor implements HasClassOrInterfaceType, HasPrimitiveType {

    private int dimensions = 0;

    private String name;

    private ClassOrInterfaceTypeDesc classOrInterfaceType;

    private PrimitiveTypeDesc primitiveType;

    public TypeDesc() {
        super(ElementType.TYPE);
    }

    public TypeDesc(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public TypeDesc(String text, int start, int stop, int line, int position) {
        super(ElementType.TYPE, text, start, stop, line, position);
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

    public boolean isPrimitiveType() {
        return primitiveType != null;
    }

    public boolean isClassOrInterfaceType() {
        return classOrInterfaceType != null;
    }

    public ClassOrInterfaceTypeDesc getClassOrInterfaceType() {
        return classOrInterfaceType;
    }

    public void setClassOrInterfaceType(ClassOrInterfaceTypeDesc classOrInterfaceType) {
        this.classOrInterfaceType = classOrInterfaceType;
    }

    public PrimitiveTypeDesc getPrimitiveType() {
        return primitiveType;
    }

    public void setPrimitiveType(PrimitiveTypeDesc primitiveType) {
        this.primitiveType = primitiveType;
    }
}
