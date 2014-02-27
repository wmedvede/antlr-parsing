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
        parser = initParser(inputStream);
        buffer = new StringBuffer(ParserUtil.readString(this.getClass().getResourceAsStream(fileName)));
    }

    protected JavaParser initParser(InputStream inputStream) throws Exception {
        ANTLRInputStream antlrInputStream = new ANTLRInputStream(inputStream);
        return initParser(antlrInputStream);
    }

    protected JavaParser initParser(final String expr) {
        final CharStream charStream = new ANTLRStringStream(expr);
        return initParser(charStream);
    }

    protected JavaParser initParser(CharStream charStream) {
        final JavaLexer lexer = new JavaLexer( charStream );
        final TokenStream tokenStream = new CommonTokenStream( lexer );
        return new JavaParser( tokenStream );
    }
}
