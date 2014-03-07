package parser;

import org.junit.Test;

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
