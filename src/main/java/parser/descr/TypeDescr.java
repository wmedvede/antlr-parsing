package parser.descr;

import java.util.List;

public class TypeDescr extends ElementDescriptor implements HasClassOrInterfaceType, HasPrimitiveType, HasDimensions {

    public TypeDescr() {
        super(ElementType.TYPE);
    }

    public TypeDescr(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public TypeDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.TYPE, text, start, stop, line, position);
    }

    public boolean isPrimitiveType() {
        return getPrimitiveType() != null;
    }

    public boolean isClassOrInterfaceType() {
        return getClassOrInterfaceType() != null;
    }

    public ClassOrInterfaceTypeDescr getClassOrInterfaceType() {
        return (ClassOrInterfaceTypeDescr)getElements2().getFirst(ElementType.CLASS_OR_INTERFACE_TYPE);
    }

    public void setClassOrInterfaceType(ClassOrInterfaceTypeDescr classOrInterfaceType) {
        getElements2().removeFirst(ElementType.CLASS_OR_INTERFACE_TYPE);
        getElements2().add(classOrInterfaceType);
    }

    public PrimitiveTypeDescr getPrimitiveType() {
        return (PrimitiveTypeDescr)getElements2().getFirst(ElementType.PRIMITIVE_TYPE);
    }

    public void setPrimitiveType(PrimitiveTypeDescr primitiveType) {
        getElements2().removeFirst(ElementType.PRIMITIVE_TYPE);
        getElements2().add(primitiveType);
    }

    @Override
    public int getDimensionsCount() {
        List<ElementDescriptor> dimensions = getElements2().getElementsByType(ElementType.DIMENSION);
        return dimensions.size();
    }

    @Override
    public TypeDescr addDimension(DimensionDescr dimensionDescr) {
        getElements2().add(dimensionDescr);
        return this;
    }

}
