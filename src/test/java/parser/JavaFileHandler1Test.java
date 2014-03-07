package parser;

import org.junit.Test;
import parser.util.ParserUtil;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;


public class JavaFileHandler1Test extends JavaFileHandlerBaseTest {

    String fileContents[] = new String[4];



    public JavaFileHandler1Test() throws Exception {
        super("JavaFileHandler1.java");

        InputStream inputStream;
        for (int i = 0; i < 4; i++) {
            inputStream = this.getClass().getResourceAsStream("JavaFileHandler1.java.delete"+i+".txt");
            fileContents[i] = ParserUtil.readString(inputStream);

        }
    }

    private void assertStrings(String a, String b) {
        for (int i = 0; i < a.length() && i < b.length(); i++) {
            assertEquals("character i: " + i + " expected: " + a.charAt(i) + " current: " + b.length(), a.charAt(i), b.charAt(i));
        }

    }

    @Test
    public void testMethodRemoval() {
        try {


            fileHandler.deleteMethod("getField2");
            System.out.print(fileHandler.build());

            //Working on this assert now
            //assertStrings(fileContents[0], fileHandler.build());
            //assertEquals(fileContents[0], fileHandler.build());


            fileHandler.deleteMethod("setField1");
            System.out.println(fileHandler.build());

            fileHandler.deleteMethod("getField1");
            System.out.println(fileHandler.build());

            fileHandler.deleteField("setField2");
            System.out.println(fileHandler.build());

            fileHandler.deleteField("field1");
            System.out.println(fileHandler.build());


            /*

            fileHandler.deleteMethod("setField2");
            fileHandler.addField("\n\tprotected String surname = null;\n");
            fileHandler.deleteMethod("setField1");
            fileHandler.deleteField("field7");
            fileHandler.addMethod("\n\tpublic static final java.lang.String echo(String msg) {\n\t\treturn msg;\n\t}\n");
            fileHandler.deleteField("field8");
            fileHandler.addField("\n\tprotected int i = 0;\n");
            fileHandler.deleteField("field6");
            fileHandler.deleteField("field9");
            fileHandler.deleteField("field11");
            fileHandler.deleteField("field14");

            fileHandler.addMember("\n\tpublic String getUserName() {\n\t\treturn surname;\n\t}\n");
            */

            String result = fileHandler.build();
            System.out.println(result);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
