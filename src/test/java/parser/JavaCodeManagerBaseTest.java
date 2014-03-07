package parser;

import org.junit.Before;
import util.ParserUtil;

import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: wmedvede
 * Date: 3/5/14
 * Time: 12:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class JavaCodeManagerBaseTest {

    String fileName;

    JavaCodeManager codeManager;

    public JavaCodeManagerBaseTest(String fileName) {
        this.fileName = fileName;
    }

    @Before
    public void preTest() throws Exception {
        InputStream inputStream = this.getClass().getResourceAsStream(fileName);
        codeManager = new JavaCodeManager(inputStream);
    }

}
