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
        return (IdentifierDescr) getElements().getFirst(ElementType.IDENTIFIER);
    }

    public VariableDeclarationDescr setIdentifier(IdentifierDescr identifier) {
        getElements().removeFirst(ElementType.IDENTIFIER);
        getElements().add(0, identifier);
        return this;
    }

    @Override
    public int getDimensionsCount() {
        List<ElementDescriptor> dimensions = getElements().getElementsByType(ElementType.DIMENSION);
        return dimensions.size();
    }

    @Override
    public VariableDeclarationDescr addDimension(DimensionDescr dimensionDescr) {
        getElements().add(dimensionDescr);
        return this;
    }

    public VariableInitializerDescr getVariableInitializer() {
        return (VariableInitializerDescr) getElements().getFirst(ElementType.VARIABLE_INITIALIZER);
    }

    public VariableDeclarationDescr setVariableInitializer(VariableInitializerDescr variableInitializer) {
        getElements().removeFirst(ElementType.VARIABLE_INITIALIZER);
        getElements().add(variableInitializer);
        return this;
    }

    public JavaTokenDescr getStartComma() {
        return (JavaTokenDescr) getElements().getFirst(ElementType.JAVA_COMMA);
    }

    public VariableDeclarationDescr setStartComma(JavaTokenDescr comma) {
        getElements().removeFirst(ElementType.JAVA_COMMA);
        getElements().add(0, comma);
        return this;
    }

    public JavaTokenDescr getEqualsSign() {
        return (JavaTokenDescr) getElements().getFirst(ElementType.JAVA_EQUALS);
    }

    public VariableDeclarationDescr setEqualsSign(JavaTokenDescr equalsSign) {
        getElements().removeFirst(ElementType.JAVA_EQUALS);
        getElements().add(equalsSign);
        return this;
    }
}