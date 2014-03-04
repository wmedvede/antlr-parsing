package parser.metadata;


import java.util.ArrayList;
import java.util.List;

public class TypeDesc extends ElementDescriptor implements HasClassOrInterfaceType, HasPrimitiveType, HasDimensions {

    private String name;

    private ClassOrInterfaceTypeDesc classOrInterfaceType;

    private PrimitiveTypeDesc primitiveType;

    private List<DimensionDesc> dimensions = new ArrayList<DimensionDesc>();

    public TypeDesc() {
        super(ElementType.TYPE);
    }

    public TypeDesc(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public TypeDesc(String text, int start, int stop, int line, int position) {
        super(ElementType.TYPE, text, start, stop, line, position);
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

    @Override
    public int getDimensionsCount() {
        return dimensions.size();
    }

    @Override
    public void addDimension(DimensionDesc dimensionDesc) {
        dimensions.add(dimensionDesc);
    }
}
