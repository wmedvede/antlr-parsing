package parser.util;

import org.antlr.runtime.*;
import parser.JavaLexer;
import parser.JavaParser;
import parser.JavaParserBase;
import parser.JavaParserBase.ParserMode;
import parser.descr.ElementDescriptor;
import parser.descr.TextTokenElementDescr;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

    public static void populateUnManagedElements(ElementDescriptor element) {
        populateUnManagedElements(element.getStart(), element);
    }

    public static void populateUnManagedElements(int startIndex, ElementDescriptor element) {

        String text;
        TextTokenElementDescr unmanagedToken;

        if (element.getElements2().size() > 0) {
            List<ElementDescriptor> originalElements = new ArrayList<ElementDescriptor>();
            originalElements.addAll(element.getElements2());

            for (ElementDescriptor child : originalElements) {
                if (startIndex < child.getStart()) {
                    unmanagedToken = new TextTokenElementDescr();
                    unmanagedToken.setStart(startIndex);
                    unmanagedToken.setStop(child.getStart()-1);
                    unmanagedToken.setSourceBuffer(child.getSourceBuffer());

                    text = unmanagedToken.getSourceBuffer().substring(unmanagedToken.getStart(), unmanagedToken.getStop() +1);
                    unmanagedToken.setText(text);

                    element.getElements2().addMemberBefore(child, unmanagedToken);
                }
                startIndex = child.getStop() + 1;

                populateUnManagedElements(child);
            }

            if (startIndex < element.getStop()) {
                unmanagedToken = new TextTokenElementDescr();
                unmanagedToken.setStart(startIndex);
                unmanagedToken.setStop(element.getStop());
                unmanagedToken.setSourceBuffer(element.getSourceBuffer());

                text = unmanagedToken.getSourceBuffer().substring(unmanagedToken.getStart(), unmanagedToken.getStop() +1);
                unmanagedToken.setText(text);
                element.getElements2().add(unmanagedToken);
            }
        }
    }

    public static String printTree(ElementDescriptor element) {
        StringBuilder result = new StringBuilder();
        if (element.getElements2().size() == 0) {
            result.append(element.getSourceBuffer().substring(element.getStart(), element.getStop() +1));
        } else {
            for (ElementDescriptor child : element.getElements2()) {
                result.append(printTree(child));
            }
        }
        return result.toString();
    }

    //temporal to not touch the parser
    public static void setSourceBufferTMP(ElementDescriptor element, StringBuilder source) {
        element.setSourceBuffer(source);
        for (ElementDescriptor child : element.getElements2()) {
            setSourceBufferTMP(child, source);
        }
    }


}
