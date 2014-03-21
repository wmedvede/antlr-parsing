package parser;

import org.junit.Test;
import parser.descr.*;
import parser.util.ParserUtil;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class JavaFileHandler1Test extends JavaFileHandlerBaseTest {

    String fileContents[] = new String[6];

    public JavaFileHandler1Test() throws Exception {
        super("JavaFileHandler1.java");

        InputStream inputStream;
        for (int i = 0; i < fileContents.length; i++) {
            inputStream = this.getClass().getResourceAsStream("JavaFileHandler1.java.result"+i+".txt");
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

            ClassDescr classDescr = fileHandler.getFileDescr().getClassDescr();
            MethodDescr methodDescr = classDescr.getMethod("getField2");
            assertNotNull(methodDescr);
            classDescr.getElements2().remove(methodDescr);

            assertStrings(fileContents[0], fileHandler.buildResult());


            methodDescr = classDescr.getMethod("setField1");
            assertNotNull(methodDescr);
            classDescr.getElements2().remove(methodDescr);
            assertStrings(fileContents[1], fileHandler.buildResult());

            methodDescr = classDescr.getMethod("getField1");
            assertNotNull(methodDescr);
            classDescr.getElements2().remove(methodDescr);
            assertStrings(fileContents[2], fileHandler.buildResult());


            FieldDescr fieldDescr = classDescr.getField("field12");
            assertNotNull(fieldDescr);
            boolean deleted = classDescr.removeField("field12");
            assertEquals(true, deleted);
            assertEquals(fileContents[3], fileHandler.buildResult());


            fieldDescr = DescriptorFactoryImpl.getInstance().createFieldDescr("public int field100 = 12;");
            StringBuilder indentStr = new StringBuilder("\n\n    ");
            TextTokenElementDescr indent = new TextTokenElementDescr("", 0, indentStr.length()-1, 1, 0);
            indent.setSourceBuffer(indentStr);

            classDescr.addField(fieldDescr);
            classDescr.getElements2().addMemberBefore(fieldDescr, indent);
            assertEquals(fileContents[4], fileHandler.buildResult());

            methodDescr = DescriptorFactoryImpl.getInstance().createMethodDescr("public java.lang.String getAddress() { return null; }");
            indentStr = new StringBuilder("\n\n    ");
            indent = new TextTokenElementDescr("", 0, indentStr.length()-1, 1, 0);
            indent.setSourceBuffer(indentStr);

            classDescr.addMethod(methodDescr);
            classDescr.getElements2().addMemberBefore(methodDescr, indent);
            assertEquals(fileContents[5], fileHandler.buildResult());

            /*

            TODO add more cases

            assertEquals(fileContents[0], fileHandler.build());


            fileHandler.deleteMethod("getField1");
            System.out.println(fileHandler.build());

            fileHandler.deleteField("setField2");
            System.out.println(fileHandler.build());

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

            String result = fileHandler.buildResult();
            //System.out.println(result);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
