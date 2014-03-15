package parser;

import org.antlr.runtime.*;
import parser.util.ParserUtil;

import java.io.InputStream;

public class JavaParserFactory {

    public static JavaParser newParser(final InputStream inputStream) throws Exception {
        StringBuilder source = ParserUtil.readStringBuilder(inputStream);
        return newParser(source.toString(), JavaParserBase.ParserMode.PARSE_CLASS);
    }

    public static JavaParser newParser(final String source, final JavaParserBase.ParserMode mode) {
        final CharStream charStream = new ANTLRStringStream(source);
        return newParser(charStream, new StringBuilder(source), mode);
    }

    public static JavaParser newParser(final String source) {
        return newParser(source, JavaParserBase.ParserMode.PARSE_CLASS);
    }

    private static JavaParser newParser(final CharStream charStream, StringBuilder source, final JavaParserBase.ParserMode mode) {
        final JavaLexer lexer = new JavaLexer( charStream );
        final TokenStream tokenStream = new CommonTokenStream( lexer );
        final JavaParser parser = new JavaParser( tokenStream, source, mode );
        return parser;
    }
}
