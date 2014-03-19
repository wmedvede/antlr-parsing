package parser;

import org.junit.Before;
import static org.junit.Assert.*;

import parser.util.ParserUtil;


import java.io.InputStream;


public class JavaFileHandlerBaseTest {

    String fileName;

    String originalFileContent;

    protected JavaFileHandler fileHandler;

    public JavaFileHandlerBaseTest(String fileName) {
        this.fileName = fileName;
    }

    @Before
    public void preTest() throws Exception {

        InputStream inputStream = this.getClass().getResourceAsStream(fileName);
        fileHandler = new JavaFileHandlerImpl(inputStream);
        inputStream.close();

        inputStream = this.getClass().getResourceAsStream(fileName);
        originalFileContent = ParserUtil.readString(inputStream);
        inputStream.close();

        //initial tests
        //after reading handler original content should be the same as file content.
        //assertEquals(originalFileContent, fileHandler.getOriginalContent());

        //if we invoke the build method without modifications
        //the generated file should be the same as originalFileContent.
        //assertEquals(originalFileContent, fileHandler.buildResult());


    }

}
