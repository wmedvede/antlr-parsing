package parser;


import org.junit.Test;
import parser.descr.MethodDescr;
import parser.util.ParserUtil;

public class ParseMethodMode1Test {

    @Test
    public void testMethodMode() {

        String sentence = "public int method1() { return -1}";

        try {

            JavaParser parser = JavaParserFactory.newParser(sentence, JavaParserBase.ParserMode.PARSE_METHOD);
            parser.methodDeclaration();
            MethodDescr methodDescr = parser.getMethodDescr();

            int i = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
