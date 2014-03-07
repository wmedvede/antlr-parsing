package parser;

import org.junit.Before;

import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: wmedvede
 * Date: 3/5/14
 * Time: 12:17 PM
 * To change this template use File | Settings | File Templates.
 */
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
