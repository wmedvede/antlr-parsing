package parser.metadata;

import java.util.ArrayList;
import java.util.List;

public class MethodDeclarationDesc extends AnnotationsContainerDesc implements HasDimensions, HasType {

    private String name;

    private int dimensions = 0;

    private TypeDesc type;

    private List<ParameterDeclarationDesc> parameters = new ArrayList<ParameterDeclarationDesc>();

    private TextTokenElementDescriptor openParenthesis;

    private TextTokenElementDescriptor closeParenthesis;

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

    public int getDimensions() {
        return dimensions;
    }

    public void addDimension() {
        dimensions++;
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

    public TextTokenElementDescriptor getOpenParenthesis() {
        return openParenthesis;
    }

    public void setOpenParenthesis(TextTokenElementDescriptor openParenthesis) {
        this.openParenthesis = openParenthesis;
    }

    public TextTokenElementDescriptor getCloseParenthesis() {
        return closeParenthesis;
    }

    public void setCloseParenthesis(TextTokenElementDescriptor closeParenthesis) {
        this.closeParenthesis = closeParenthesis;
    }
}
