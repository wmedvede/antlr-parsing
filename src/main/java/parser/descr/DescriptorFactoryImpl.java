package parser.descr;


import parser.JavaParser;
import parser.JavaParserBase;
import parser.JavaParserBase.ParserMode;
import parser.JavaParserFactory;
import parser.util.ParserUtil;

public class DescriptorFactoryImpl implements DescriptorFactory {

    public static DescriptorFactory getInstance() {
        return new DescriptorFactoryImpl();
    }

    @Override
    public MethodDescr createMethodDescr(String source) throws Exception {
        JavaParser parser = JavaParserFactory.newParser(source, ParserMode.PARSE_METHOD);
        parser.methodDeclaration();
        MethodDescr methodDescr = parser.getMethodDescr();
        //TODO the parser should set the source for the elements
        ParserUtil.setSourceBufferTMP(methodDescr, parser.getSourceBuffer());
        ParserUtil.populateUnManagedElements(methodDescr);
        ParserUtil.setSourceBufferTMP(methodDescr, parser.getSourceBuffer());
        return methodDescr;
    }

    @Override
    public FieldDescr createFieldDescr(String source) throws Exception {
        JavaParser parser = JavaParserFactory.newParser(source, ParserMode.PARSE_FIELD);
        parser.fieldDeclaration();
        FieldDescr fieldDescr = parser.getFieldDescr();
        //TODO the parser should set the source the his children
        ParserUtil.setSourceBufferTMP(fieldDescr, parser.getSourceBuffer());
        ParserUtil.populateUnManagedElements(fieldDescr);
        ParserUtil.setSourceBufferTMP(fieldDescr, parser.getSourceBuffer());
        return fieldDescr;
    }
}
