package parser.descr;


import java.util.ArrayList;
import java.util.List;

public class TypeDescr extends ElementDescriptor implements HasClassOrInterfaceType, HasPrimitiveType, HasDimensions {

    private String name;

    private ClassOrInterfaceTypeDescr classOrInterfaceType;

    private PrimitiveTypeDescr primitiveType;

    private List<DimensionDescr> dimensions = new ArrayList<DimensionDescr>();

    public TypeDescr() {
        super(ElementType.TYPE);
    }

    public TypeDescr(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public TypeDescr(String text, int start, int stop, int line, int position) {
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

    public ClassOrInterfaceTypeDescr getClassOrInterfaceType() {
        return classOrInterfaceType;
    }

    public void setClassOrInterfaceType(ClassOrInterfaceTypeDescr classOrInterfaceType) {
        this.classOrInterfaceType = classOrInterfaceType;
    }

    public PrimitiveTypeDescr getPrimitiveType() {
        return primitiveType;
    }

    public void setPrimitiveType(PrimitiveTypeDescr primitiveType) {
        this.primitiveType = primitiveType;
    }

    @Override
    public int getDimensionsCount() {
        return dimensions.size();
    }

    @Override
    public void addDimension(DimensionDescr dimensionDescr) {
        dimensions.add(dimensionDescr);
    }
}
