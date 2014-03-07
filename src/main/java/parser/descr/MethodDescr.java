package parser.descr;

import java.util.ArrayList;
import java.util.List;

public class MethodDescr extends AnnotationsContainerDescr implements HasDimensions, HasType {

    private String name;

    private List<DimensionDescr> dimensions = new ArrayList<DimensionDescr>();

    private TypeDescr type;

    private List<ParameterDescr> parameters = new ArrayList<ParameterDescr>();

    private TextTokenElementDescr paramsStart;

    private TextTokenElementDescr paramsStop;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDimensions(List<DimensionDescr> dimensions) {
        this.dimensions = dimensions;
    }

    public int getDimensionsCount() {
        return dimensions.size();
    }

    public void addDimension(DimensionDescr dimensionDescr) {
        dimensions.add(dimensionDescr);
    }

    @Override
    public void setType(TypeDescr type) {
        this.type = type;
    }

    @Override
    public TypeDescr getType() {
        return type;
    }

    public List<ParameterDescr> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterDescr> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(ParameterDescr parameter) {
        parameters.add(parameter);
    }

    public TextTokenElementDescr getParamsStart() {
        return paramsStart;
    }

    public void setParamsStart(TextTokenElementDescr paramsStart) {
        this.paramsStart = paramsStart;
    }

    public TextTokenElementDescr getParamsStop() {
        return paramsStop;
    }

    public void setParamsStop(TextTokenElementDescr paramsStop) {
        this.paramsStop = paramsStop;
    }
}
