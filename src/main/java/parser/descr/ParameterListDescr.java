package parser.descr;


import java.util.ArrayList;
import java.util.List;

public class ParameterListDescr extends ElementDescriptor {

    public ParameterListDescr() {
        super(ElementType.PARAMETER_LIST);
    }

    public ParameterListDescr(String text, int start, int line, int position) {
        this(text, start, -1, line, position);
    }

    public ParameterListDescr(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public ParameterListDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.PARAMETER_LIST, text, start, stop, line, position);
    }

    public ParameterListDescr addParameter(ParameterDescr param) {
        getElements2().add(param);
        return this;
    }

    public List<ParameterDescr> getParameters() {
        List<ParameterDescr> params = new ArrayList<ParameterDescr>();
        for (ElementDescriptor element : getElements2()) {
            if (ElementType.NORMAL_PARAMETER == element.getElementType() || ElementType.ELLIPSIS_PARAMETER == element.getElementType()) {
                params.add((ParameterDescr)element);

            }
        }
        return params;
    }
}
