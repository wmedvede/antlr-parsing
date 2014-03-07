package parser.util;

import org.antlr.runtime.*;
import parser.JavaLexer;
import parser.JavaParser;
import parser.descr.ElementDescriptor;

import java.io.*;

public class ParserUtil {

    public static String readElement(StringBuffer stringBuffer, ElementDescriptor elementDescriptor) {
        if (stringBuffer == null || elementDescriptor == null) return null;
        return stringBuffer.substring(elementDescriptor.getStart(), elementDescriptor.getStop()+1);
    }

    public static String readString(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String line;
        String lineSeparator = System.getProperty("line.separator");
        while ((line = reader.readLine()) != null) {
            out.append(line);
            out.append(lineSeparator);
        }
        reader.close();
        return out.toString();
    }

    public static JavaParser initParser(final InputStream inputStream) throws Exception {
        ANTLRInputStream antlrInputStream = new ANTLRInputStream(inputStream);
        return initParser(antlrInputStream);
    }

    public static JavaParser initParser(final String expr) {
        final CharStream charStream = new ANTLRStringStream(expr);
        return initParser(charStream);
    }

    public static JavaParser initParser(final CharStream charStream) {
        final JavaLexer lexer = new JavaLexer( charStream );
        final TokenStream tokenStream = new CommonTokenStream( lexer );
        return new JavaParser( tokenStream );
    }

}