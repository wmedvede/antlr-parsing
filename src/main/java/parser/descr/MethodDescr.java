package parser.descr;

import java.util.List;

public class MethodDescr extends ModifiersContainerDescr implements HasDimensions, HasType {

    public MethodDescr() {
        super(ElementType.METHOD);
    }

    public MethodDescr(String text, int start, int line, int position) {
        this(text, start, -1, line, position);
    }

    public MethodDescr(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public MethodDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.METHOD, text, start, stop, line, position);
    }

    public IdentifierDescr getIdentifier() {
        return (IdentifierDescr) getElements().getFirst(ElementType.IDENTIFIER);
    }

    public MethodDescr setIdentifier(IdentifierDescr identifier) {
        getElements().removeFirst(ElementType.IDENTIFIER);
        getElements().add(identifier);
        return this;
    }

    public boolean isConstructor() {
        return getType() == null;
    }

    @Override
    public int getDimensionsCount() {
        List<ElementDescriptor> dimensions = getElements().getElementsByType(ElementType.DIMENSION);
        return dimensions.size();
    }

    @Override
    public MethodDescr addDimension(DimensionDescr dimensionDescr) {
        getElements().add(dimensionDescr);
        return this;
    }

    public TypeDescr getType() {
        return (TypeDescr) getElements().getFirst(ElementType.TYPE);
    }

    public MethodDescr setType(TypeDescr type) {
        getElements().removeFirst(ElementType.TYPE);
        getElements().add(type);
        return this;
    }

    public ParameterListDescr getParamsList() {
        return (ParameterListDescr) getElements().getFirst(ElementType.PARAMETER_LIST);
    }

    public void setParamsList(ParameterListDescr params) {
        getElements().removeFirst(ElementType.PARAMETER_LIST);
        getElements().add(params);
    }

    public JavaTokenDescr getParamsStartParen() {
        return (JavaTokenDescr) getElements().getFirst(ElementType.JAVA_LPAREN);
    }

    public void setParamsStartParen(JavaTokenDescr paramsStart) {
        getElements().removeFirst(ElementType.JAVA_LPAREN);
        getElements().add(paramsStart);
    }

    public JavaTokenDescr getParamsStopParen() {
        return (JavaTokenDescr) getElements().getFirst(ElementType.JAVA_RPAREN);
    }

    public void setParamsStopParen(JavaTokenDescr paramsStop) {
        getElements().removeFirst(ElementType.JAVA_RPAREN);
        getElements().add(paramsStop);
    }
}
