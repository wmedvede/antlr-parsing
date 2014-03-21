package parser;

import parser.descr.*;
import parser.util.ParserUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaFileHandlerImpl {

    private StringBuilder source = null;

    private JavaParser parser;

    private FileDescr fileDescr;

    public JavaFileHandlerImpl(InputStream inputStream) throws Exception {
        //TODO implement better exceptions handling
        source = new StringBuilder(ParserUtil.readString(inputStream));
        parseSource();
    }

    public FileDescr getFileDescr() {
        return fileDescr;
    }

    public String getOriginalContent() {
        return source.toString();
    }

    private void parseSource() throws Exception {
        parser = JavaParserFactory.newParser(source.toString());
        parser.compilationUnit();
        fileDescr = parser.getFileDescr();
        ParserUtil.setSourceBufferTMP(fileDescr, parser.getSourceBuffer());
        ParserUtil.populateUnManagedElements(fileDescr);
    }

    public String buildResult() {
        return ParserUtil.printTree(fileDescr);
    }

    /*
    public void populateUnManagedElements(ElementDescriptor element) {
        populateUnManagedElements(element.getStart(), element);
    }


    public void populateUnManagedElements(int startIndex, ElementDescriptor element) {

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

*/

    /*
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
    public void setSourceBufferTMP(ElementDescriptor element, StringBuilder source) {
        element.setSourceBuffer(source);
        for (ElementDescriptor child : element.getElements2()) {
            setSourceBufferTMP(child, source);
        }
    }
    */

}