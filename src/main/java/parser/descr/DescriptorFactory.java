package parser.descr;


import parser.descr.AnnotationDescr;
import parser.descr.FieldDescr;
import parser.descr.MethodDescr;

public interface DescriptorFactory {

    MethodDescr createMethodDescr(String source) throws Exception;

    FieldDescr createFieldDescr(String source) throws Exception;

}
