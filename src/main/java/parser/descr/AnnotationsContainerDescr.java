package parser.descr;

import java.util.ArrayList;
import java.util.List;

public class AnnotationsContainerDescr extends ModifiersContainerDescr implements HasAnnotations {

    private List<AnnotationDescr> annotations = new ArrayList<AnnotationDescr>();

    public AnnotationsContainerDescr(ElementType elementType) {
        super(elementType);
    }

    public AnnotationsContainerDescr(ElementType elementType, String text, int start, int line, int position) {
        super(elementType, text, start, line, position);
    }

    public AnnotationsContainerDescr(ElementType elementType, String text, int start, int stop) {
        super(elementType, text, start, stop);
    }

    public AnnotationsContainerDescr(ElementType elementType, String text, int start, int stop, int line, int position) {
        super(elementType, text, start, stop, line, position);
    }

    @Override
    public List<AnnotationDescr> getAnnotations() {
        return annotations;
    }

    @Override
    public void addAnnotation(AnnotationDescr annotationDesc) {
        annotations.add(annotationDesc);
    }
}
