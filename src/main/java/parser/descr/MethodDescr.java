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
        return (IdentifierDescr)getElements2().getFirst(ElementType.IDENTIFIER);
    }

    public MethodDescr setIdentifier(IdentifierDescr identifier) {
        getElements2().removeFirst(ElementType.IDENTIFIER);
        getElements2().add(identifier);
        return this;
    }

    @Override
    public int getDimensionsCount() {
        List<ElementDescriptor> dimensions = getElements2().getElementsByType(ElementType.DIMENSION);
        return dimensions.size();
    }

    @Override
    public MethodDescr addDimension(DimensionDescr dimensionDescr) {
        getElements2().add(dimensionDescr);
        return this;
    }

    public TypeDescr getType() {
        return (TypeDescr)getElements2().getFirst(ElementType.TYPE);
    }

    public MethodDescr setType(TypeDescr type) {
        getElements2().removeFirst(ElementType.TYPE);
        getElements2().add(type);
        return this;
    }

    public ParameterListDescr getParamsList() {
        return (ParameterListDescr)getElements2().getFirst(ElementType.PARAMETER_LIST);
    }

    public void setParamsList(ParameterListDescr params) {
        getElements2().removeFirst(ElementType.PARAMETER_LIST);
        getElements2().add(params);
    }

    public JavaTokenDescr getParamsStartParen() {
        return (JavaTokenDescr)getElements2().getFirst(ElementType.JAVA_LPAREN);
    }

    public void setParamsStartParen(JavaTokenDescr paramsStart) {
        getElements2().removeFirst(ElementType.JAVA_LPAREN);
        getElements2().add(paramsStart);
    }

    public JavaTokenDescr getParamsStopParen() {
        return (JavaTokenDescr)getElements2().getFirst(ElementType.JAVA_RPAREN);
    }

    public void setParamsStopParen(JavaTokenDescr paramsStop) {
        getElements2().removeFirst(ElementType.JAVA_RPAREN);
        getElements2().add(paramsStop);
    }
}
