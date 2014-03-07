package parser;

import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: wmedvede
 * Date: 2/28/14
 * Time: 8:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class TypeParsing1Test extends JavaParserBaseTest {

    public TypeParsing1Test() {
        super("TypeParsing1.java");
    }

    @Test
    public void testTypeParsing() {

        try {
            parser.compilationUnit();
            int i = 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
