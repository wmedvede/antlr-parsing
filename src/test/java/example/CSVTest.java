package example;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;


public class CSVTest {


    @Test
    @Ignore
    public void doTest() throws Exception {
        // the input sourceBuffer
        String source =
                "value1,value2,\"value3.1,\"\",value3.2\"" + "\n" +
                        "\"line\nbreak\",Bbb,end";

        // create an instance of the lexer
        CSVLexer lexer = new CSVLexer(new ANTLRStringStream(source));

        // wrap a token-stream around the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // create the parser
        CSVParser parser = new CSVParser(tokens);

        // invoke the entry point of our grammar
        parser.file();
    }

    @Test
    @Ignore
    public void doTest2() throws Exception {
        // the input sourceBuffer
        String source =
                "aaa,bbb,ccc" + "\n" +
                        "\"d,\"\"d\",eee,fff";

        // create an instance of the lexer
        CSVLexer lexer = new CSVLexer(new ANTLRStringStream(source));

        // wrap a token-stream around the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // create the parser
        CSVParser parser = new CSVParser(tokens);

        // invoke the entry point of our grammar

        List<List<String>> data = parser.file();

        // display the contents of the CSV sourceBuffer
        for(int r = 0; r < data.size(); r++) {
            List<String> row = data.get(r);
            for(int c = 0; c < row.size(); c++) {
                System.out.println("(row=" + (r+1) + ",col=" + (c+1) + ") = " + row.get(c));
            }
        }


    }

}
