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

    public boolean isVoidType() {
        return getVoidType() != null;
    }

    public ClassOrInterfaceTypeDescr getClassOrInterfaceType() {
        return (ClassOrInterfaceTypeDescr) getElements().getFirst(ElementType.CLASS_OR_INTERFACE_TYPE);
    }

    public void setClassOrInterfaceType(ClassOrInterfaceTypeDescr classOrInterfaceType) {
        getElements().removeFirst(ElementType.CLASS_OR_INTERFACE_TYPE);
        getElements().add(classOrInterfaceType);
    }

    public PrimitiveTypeDescr getPrimitiveType() {
        return (PrimitiveTypeDescr) getElements().getFirst(ElementType.PRIMITIVE_TYPE);
    }

    public void setPrimitiveType(PrimitiveTypeDescr primitiveType) {
        getElements().removeFirst(ElementType.PRIMITIVE_TYPE);
        getElements().add(primitiveType);
    }

    public void setVoidType(JavaTokenDescr voidToken) {
        getElements().removeFirst(ElementType.JAVA_VOID);
        getElements().add(voidToken);
    }

    public JavaTokenDescr getVoidType() {
        return (JavaTokenDescr) getElements().getFirst(ElementType.JAVA_VOID);
    }

    @Override
    public int getDimensionsCount() {
        List<ElementDescriptor> dimensions = getElements().getElementsByType(ElementType.DIMENSION);
        return dimensions.size();
    }

    @Override
    public TypeDescr addDimension(DimensionDescr dimensionDescr) {
        getElements().add(dimensionDescr);
        return this;
    }

}
