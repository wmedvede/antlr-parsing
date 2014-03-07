package parser.descr;

import java.util.ArrayList;
import java.util.List;

public class NormalParameterDescr extends ParameterDescr implements HasDimensions {

    private List<DimensionDescr> dimensions = new ArrayList<DimensionDescr>();

    public NormalParameterDescr() {
        super(ElementType.NORMAL_PARAMETER);
    }

    public NormalParameterDescr(String text, int start, int stop, String name) {
        this(text, start, stop, -1, -1, name);
    }

    public NormalParameterDescr(String text, int start, int line, int position, String name) {
        this(text, start, -1, line, position, name);
    }

    public NormalParameterDescr(String text, int start, int stop, int line, int position, String name) {
        super(ElementType.NORMAL_PARAMETER, text, start, stop, line, position, name);
    }

    @Override
    public int getDimensionsCount() {
        return dimensions.size();
    }

    @Override
    public void addDimension(DimensionDescr dimensionDescr) {
        dimensions.add(dimensionDescr);
    }
}
