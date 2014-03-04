package parser.metadata;

import java.util.ArrayList;
import java.util.List;

public class MethodDeclarationDesc extends AnnotationsContainerDesc implements HasDimensions, HasType {

    private String name;

    private List<DimensionDesc> dimensions = new ArrayList<DimensionDesc>();

    private TypeDesc type;

    private List<ParameterDeclarationDesc> parameters = new ArrayList<ParameterDeclarationDesc>();

    private TextTokenElementDescriptor paramsStart;

    private TextTokenElementDescriptor paramsStop;

    public MethodDeclarationDesc() {
        super(ElementType.METHOD);
    }

    public MethodDeclarationDesc(String text, int start, int line, int position) {
        this(text, start, -1, line, position);
    }

    public MethodDeclarationDesc(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public MethodDeclarationDesc(String text, int start, int stop, int line, int position) {
        super(ElementType.METHOD, text, start, stop, line, position);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDimensions(List<DimensionDesc> dimensions) {
        this.dimensions = dimensions;
    }

    public int getDimensionsCount() {
        return dimensions.size();
    }

    public void addDimension(DimensionDesc dimensionDesc) {
        dimensions.add(dimensionDesc);
    }

    @Override
    public void setType(TypeDesc type) {
        this.type = type;
    }

    @Override
    public TypeDesc getType() {
        return type;
    }

    public List<ParameterDeclarationDesc> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterDeclarationDesc> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(ParameterDeclarationDesc parameter) {
        parameters.add(parameter);
    }

    public TextTokenElementDescriptor getParamsStart() {
        return paramsStart;
    }

    public void setParamsStart(TextTokenElementDescriptor paramsStart) {
        this.paramsStart = paramsStart;
    }

    public TextTokenElementDescriptor getParamsStop() {
        return paramsStop;
    }

    public void setParamsStop(TextTokenElementDescriptor paramsStop) {
        this.paramsStop = paramsStop;
    }
}
