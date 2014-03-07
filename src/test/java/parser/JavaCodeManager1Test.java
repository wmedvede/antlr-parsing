package parser;

import org.junit.Test;
import static org.junit.Assert.assertEquals;


/**
 * Created with IntelliJ IDEA.
 * User: wmedvede
 * Date: 3/5/14
 * Time: 12:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class JavaCodeManager1Test extends JavaCodeManagerBaseTest {

    public JavaCodeManager1Test() {
        super("JavaCodeManager1.java");
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
