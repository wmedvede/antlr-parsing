package parser;

import org.junit.Test;
import parser.descr.ClassDescr;
import parser.descr.DescriptorFactoryImpl;
import parser.descr.FieldDescr;
import parser.util.ParserUtil;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;


public class JavaFileHandler2Test extends JavaFileHandlerBaseTest {

    String fileContents[] = new String[6];

    public JavaFileHandler2Test() throws Exception {
        super("JavaFileHandler2.txt");
    }

    private void assertStrings(String a, String b) {
        for (int i = 0; i < a.length() && i < b.length(); i++) {
            assertEquals("character i: " + i + " expected: " + a.charAt(i) + " current: " + b.length(), a.charAt(i), b.charAt(i));
        }
    }

    @Test
    public void test() {
        try {




            ((JavaFileHandlerImpl)fileHandler).setSourceBufferTMP(fileHandler.getFileDescr(), new StringBuilder(fileHandler.getOriginalContent()));

            String tree1 = ((JavaFileHandlerImpl)fileHandler).printTree(fileHandler.getFileDescr());



            ((JavaFileHandlerImpl)fileHandler).populateUnManagedElements(fileHandler.getFileDescr());
            ((JavaFileHandlerImpl)fileHandler).setSourceBufferTMP(fileHandler.getFileDescr(), new StringBuilder(fileHandler.getOriginalContent()));

            ClassDescr classDescr = fileHandler.getFileDescr().getClassDescr();




            FieldDescr field = DescriptorFactoryImpl.getInstance().createFieldDescr("\n\n\tpublic /*eso*/static   int value  = (2+4), otro=1  /**/ ; /**/");

            ((JavaFileHandlerImpl)fileHandler).setSourceBufferTMP(field, field.getSourceBuffer());
            ((JavaFileHandlerImpl)fileHandler).populateUnManagedElements(0, field);
            ((JavaFileHandlerImpl)fileHandler).setSourceBufferTMP(field, field.getSourceBuffer());

            classDescr.addField(field);
            classDescr.addField(field);


            String tree = ((JavaFileHandlerImpl)fileHandler).printTree(fileHandler.getFileDescr());
            System.out.println(tree);

            classDescr.getElements2().remove(field);
            classDescr.getElements2().remove(field);

            tree = ((JavaFileHandlerImpl)fileHandler).printTree(fileHandler.getFileDescr());
            System.out.println(tree);



            assertEquals(originalFileContent, tree);
            int i = 0;



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
