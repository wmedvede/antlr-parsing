package parser.descr;

import java.util.List;

public class VariableDeclarationDescr extends ElementDescriptor implements HasDimensions {

    public VariableDeclarationDescr() {
        super(ElementType.VARIABLE);
    }

    public VariableDeclarationDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.VARIABLE, text, start, stop, line, position);
    }

    public VariableDeclarationDescr(String text, int start, int line, int position) {
        this(text, start, -1, line, position);
    }

    public IdentifierDescr getIdentifier() {
        return (IdentifierDescr)getElements2().getFirst(ElementType.IDENTIFIER);
    }

    public VariableDeclarationDescr setIdentifier(IdentifierDescr identifier) {
        getElements2().removeFirst(ElementType.IDENTIFIER);
        getElements2().add(0, identifier);
        return this;
    }

    @Override
    public int getDimensionsCount() {
        List<ElementDescriptor> dimensions = getElements2().getElementsByType(ElementType.DIMENSION);
        return dimensions.size();
    }

    @Override
    public VariableDeclarationDescr addDimension(DimensionDescr dimensionDescr) {
        getElements2().add(dimensionDescr);
        return this;
    }

    public VariableInitializerDescr getVariableInitializer() {
        return (VariableInitializerDescr)getElements2().getFirst(ElementType.VARIABLE_INITIALIZER);
    }

    public VariableDeclarationDescr setVariableInitializer(VariableInitializerDescr variableInitializer) {
        getElements2().removeFirst(ElementType.VARIABLE_INITIALIZER);
        getElements2().add(variableInitializer);
        return this;
    }

    public JavaTokenDescr getStartComma() {
        return (JavaTokenDescr)getElements2().getFirst(ElementType.JAVA_COMMA);
    }

    public VariableDeclarationDescr setStartComma(JavaTokenDescr comma) {
        getElements2().removeFirst(ElementType.JAVA_COMMA);
        getElements2().add(0, comma);
        return this;
    }

    public JavaTokenDescr getEqualsSign() {
        return (JavaTokenDescr)getElements2().getElementsByType(ElementType.JAVA_EQUALS);
    }

    public VariableDeclarationDescr setEqualsSign(JavaTokenDescr equalsSign) {
        getElements2().removeFirst(ElementType.JAVA_EQUALS);
        getElements2().add(equalsSign);
        return this;
    }
}