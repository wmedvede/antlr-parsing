package parser;

import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class JavaFileHandler1Test extends JavaFileHandlerBaseTest {

    public JavaFileHandler1Test() {
        super("JavaFileHandler1.java");
    }

    @Test
    public void testMethodRemoval() {
        try {

            /*
            codeManager.deleteMethod("getField2");
            System.out.println(codeManager.build());

            codeManager.deleteMethod("setField1");
            System.out.println(codeManager.build());

            codeManager.deleteMethod("getField1");
            System.out.println(codeManager.build());

            codeManager.deleteField("setField2");
            System.out.println(codeManager.build());

            codeManager.deleteField("field1");
            System.out.println(codeManager.build());

*/

            /*

            codeManager.deleteMethod("setField2");
            codeManager.addField("\n\tprotected String surname = null;\n");
            codeManager.deleteMethod("setField1");
            codeManager.deleteField("field7");
            codeManager.addMethod("\n\tpublic static final java.lang.String echo(String msg) {\n\t\treturn msg;\n\t}\n");
            codeManager.deleteField("field8");
            codeManager.addField("\n\tprotected int i = 0;\n");
            codeManager.deleteField("field6");
            codeManager.deleteField("field9");
            codeManager.deleteField("field11");
            codeManager.deleteField("field14");

            codeManager.addMember("\n\tpublic String getUserName() {\n\t\treturn surname;\n\t}\n");
            */

            String result = codeManager.build();
            System.out.println(result);



            assertEquals(codeManager.getOriginalContent(), result);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
