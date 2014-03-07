package parser;

import org.junit.Before;

import java.io.InputStream;


public class JavaFileHandlerBaseTest {

    String fileName;

    JavaFileHandler codeManager;

    public JavaFileHandlerBaseTest(String fileName) {
        this.fileName = fileName;
    }

    @Before
    public void preTest() throws Exception {
        InputStream inputStream = this.getClass().getResourceAsStream(fileName);
        codeManager = new JavaFileHandler(inputStream);
    }

}
