package parser.util;

import org.antlr.runtime.*;
import parser.JavaLexer;
import parser.JavaParser;
import parser.JavaParserBase;
import parser.JavaParserBase.ParserMode;
import parser.descr.ElementDescriptor;

import java.io.*;

public class ParserUtil {

    public static String readElement(StringBuffer stringBuffer, ElementDescriptor elementDescriptor) {
        if (stringBuffer == null || elementDescriptor == null) return null;
        return stringBuffer.substring(elementDescriptor.getStart(), elementDescriptor.getStop()+1);
    }

    public static String readString(InputStream in) throws IOException {
        return readStringBuilder(in).toString();
    }

    public static StringBuilder readStringBuilder(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String line;
        String lineSeparator = System.getProperty("line.separator");
        line = reader.readLine();
        if (line != null) {
            out.append(line);
            while ((line = reader.readLine()) != null) {
                out.append(lineSeparator);
                out.append(line);
            }
        }
        return out;
    }

}
