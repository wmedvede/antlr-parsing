package parser.descr;

import java.util.List;

public interface HasAnnotations {

    List<AnnotationDescr> getAnnotations();

    void addAnnotation(AnnotationDescr annotationDescr);

}
