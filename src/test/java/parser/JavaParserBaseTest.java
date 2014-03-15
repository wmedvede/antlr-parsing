package parser;

import org.junit.Before;
import static org.junit.Assert.*;
import parser.util.ParserUtil;

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
        parser = JavaParserFactory.newParser(inputStream);
        buffer = new StringBuffer(ParserUtil.readString(this.getClass().getResourceAsStream(fileName)));

        parser.compilationUnit();
    }

    protected void assertClass() {
        assertNotNull(parser.getFileDescr());
        assertNotNull(parser.getFileDescr().getClassDescr());
    }

}
