package parser.metadata;

import java.util.ArrayList;
import java.util.List;

public class NormalParameterDeclarationDesc extends ParameterDeclarationDesc implements HasDimensions {

    private List<DimensionDesc> dimensions = new ArrayList<DimensionDesc>();

    public NormalParameterDeclarationDesc() {
        super(ElementType.NORMAL_PARAMETER);
    }

    public NormalParameterDeclarationDesc(String text, int start, int stop, String name) {
        this(text, start, stop, -1, -1, name);
    }

    public NormalParameterDeclarationDesc(String text, int start, int line, int position, String name) {
        this(text, start, -1, line, position, name);
    }

    public NormalParameterDeclarationDesc(String text, int start, int stop, int line, int position, String name) {
        super(ElementType.NORMAL_PARAMETER, text, start, stop, line, position, name);
    }

    @Override
    public int getDimensionsCount() {
        return dimensions.size();
    }

    @Override
    public void addDimension(DimensionDesc dimensionDesc) {
        dimensions.add(dimensionDesc);
    }
}
