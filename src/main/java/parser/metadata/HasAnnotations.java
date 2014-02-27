package parser.metadata;

import java.util.List;

public interface HasAnnotations {

    List<AnnotationDeclarationDesc> getAnnotations();

    void addAnnotation(AnnotationDeclarationDesc annotationDesc);

}
