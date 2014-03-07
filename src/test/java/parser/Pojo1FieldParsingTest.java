package parser;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import parser.descr.FieldDescr;
import util.ParserUtil;

import java.util.List;

public class Pojo1FieldParsingTest extends JavaParserBaseTest {

    public Pojo1FieldParsingTest() {
        super("Pojo1.java");
    }

    @Test
    public void testFieldsReading1() {
        try {

            assertClass();
            List<FieldDescr> fields = parser.getFileDescr().getClassDescr().getFields();

            assertEquals(3, fields.size());
            String[] fieldSentences = new String[3];

            fieldSentences[0] = "private /*comment2*/ java.lang.String name  ;";
            fieldSentences[1] = "public  static  int a  = 3 ,   b =   4         ;";
            fieldSentences[2] = "java.util.List<List<String>> list;";

            for (int i = 0; i < fields.size() && i < fieldSentences.length; i++) {
                assertEquals(fieldSentences[i], ParserUtil.readElement(buffer, fields.get(i)));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
