package parser.descr;

import java.util.List;

public class NormalParameterDescr extends ParameterDescr implements HasDimensions {

    public NormalParameterDescr() {
        super(ElementType.NORMAL_PARAMETER);
    }

    public NormalParameterDescr(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public NormalParameterDescr(String text, int start, int line, int position) {
        this(text, start, -1, line, position);
    }

    public NormalParameterDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.NORMAL_PARAMETER, text, start, stop, line, position);
    }

    @Override
    public int getDimensionsCount() {
        List<ElementDescriptor> dimensions = getElements2().getElementsByType(ElementType.DIMENSION);
        return dimensions.size();
    }

    @Override
    public NormalParameterDescr addDimension(DimensionDescr dimensionDescr) {
        getElements2().add(dimensionDescr);
        return this;
    }

}
