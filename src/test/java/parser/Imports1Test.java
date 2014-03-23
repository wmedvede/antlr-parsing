package parser;


import org.junit.Test;
import parser.descr.ImportDescr;
import parser.util.ParserUtil;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Imports1Test extends JavaParserBaseTest {

    public Imports1Test() {
        super("Imports1.java");
        init();
    }

    private List<String> importSentences = new ArrayList<String>();
    private List<String> importNames = new ArrayList<String>();

    @Test
    public void testImportSentencesReading() {
        try {
            assertClass();
            List<ImportDescr> imports = parser.getFileDescr().getImports();
            assertEquals(importSentences.size(), imports.size());

            //test import definition sentences
            int i = 0;
            for (String importSentence : importSentences) {
                assertEquals(importSentences.get(i), ParserUtil.readElement(buffer, imports.get(i)));
                i++;
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    public void testImportNames() {
        try {
            assertClass();
            List<ImportDescr> imports = parser.getFileDescr().getImports();
            assertEquals(importSentences.size(), imports.size());

            //test import names
            int i = 0;
            for (String importName : importNames) {
                assertEquals(importNames.get(i), imports.get(i).getName(true));
                i++;
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }

    /*
    @Test
    public void testClassFields() {
        try {
            assertClass();
            List<FieldDescr> fields = parser.getFileDescr().getClassDescr().getFields();
            for (int i = 0; i < expectedFields.size(); i++) {
                assertEqualsFieldDeclaration(buffer, expectedFields.get(i), fields.get(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed");
        }
    }
    */

    private void init() {

        importSentences.add("import parser.*;");
        importNames.add("parser.*");

        importSentences.add("import parser.JavaParser;");
        importNames.add("parser.JavaParser");

        importSentences.add("import java.util.*;");
        importNames.add("java.util.*");

        importSentences.add("import java.util.AbstractList;");
        importNames.add("java.util.AbstractList");

        importSentences.add("import static org.junit.Assert.assertArrayEquals;");
        importNames.add("org.junit.Assert.assertArrayEquals");

        importSentences.add("import static org.junit.Assert.*;");
        importNames.add("org.junit.Assert.*");
    }

}
