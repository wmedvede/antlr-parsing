package parser.metadata;

import java.util.ArrayList;
import java.util.List;

public class AnnotationsContainerDesc extends ModifiersContainerDesc implements HasAnnotations {

    private List<AnnotationDeclarationDesc> annotations = new ArrayList<AnnotationDeclarationDesc>();

    public AnnotationsContainerDesc(ElementType elementType) {
        super(elementType);
    }

    public AnnotationsContainerDesc(ElementType elementType, String text, int start, int line, int position) {
        super(elementType, text, start, line, position);
    }

    public AnnotationsContainerDesc(ElementType elementType, String text, int start, int stop) {
        super(elementType, text, start, stop);
    }

    public AnnotationsContainerDesc(ElementType elementType, String text, int start, int stop, int line, int position) {
        super(elementType, text, start, stop, line, position);
    }

    @Override
    public List<AnnotationDeclarationDesc> getAnnotations() {
        return annotations;
    }

    @Override
    public void addAnnotation(AnnotationDeclarationDesc annotationDesc) {
        annotations.add(annotationDesc);
    }
}
