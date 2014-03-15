package parser;

import org.junit.Test;
import parser.descr.FieldDescr;
import parser.descr.MethodDescr;
import parser.util.ParserUtil;

/**
 * Created with IntelliJ IDEA.
 * User: wmedvede
 * Date: 3/11/14
 * Time: 4:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParseFieldMode1Test {

    @Test
    public void testFieldMode() {

        String sentence = "public int field1";

        try {

            JavaParser parser = JavaParserFactory.newParser(sentence, JavaParserBase.ParserMode.PARSE_FIELD);
            parser.fieldDeclaration();
            FieldDescr fieldDescr = parser.getFieldDescr();

            int i = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
