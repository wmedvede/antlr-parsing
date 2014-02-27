package parser;


import org.junit.Test;
import parser.metadata.MethodDeclarationDesc;
import util.ParserUtil;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MehodParsing1Test extends JavaParserBaseTest {

    public MehodParsing1Test() {
        super("MethodParsing1.java");
    }

    @Test
    public void testMethodsSentencesReading() {

        try {
            List<String>methodSentences = new ArrayList<String>();
            methodSentences.add("public String getName() { return name; }");
            methodSentences.add("public void setName(String name) { this.name = name; }");
            methodSentences.add("public String getSurname() { return surname; }");
            methodSentences.add("public void setSurname(String surname) { this.surname = surname; }");

            parser.compilationUnit();
            List<MethodDeclarationDesc> methods  = parser.getMethods();

            int i = 0;
            for (String methodSentence : methodSentences) {
                assertEquals(methodSentences.get(i), ParserUtil.readElement(buffer, methods.get(i)));
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
