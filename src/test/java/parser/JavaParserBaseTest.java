package parser;

import org.antlr.runtime.*;
import org.junit.Before;
import util.ParserUtil;

import java.io.InputStream;

public class JavaParserBaseTest {

    JavaParser parser;

    String fileName;

    StringBuffer buffer;

    public JavaParserBaseTest(String fileName) {
        this.fileName = fileName;
    }

    @Before
    public void preTest() throws Exception {
        InputStream inputStream = this.getClass().getResourceAsStream(fileName);
        parser = ParserUtil.initParser(inputStream);
        buffer = new StringBuffer(ParserUtil.readString(this.getClass().getResourceAsStream(fileName)));

        parser.compilationUnit();
    }

}
