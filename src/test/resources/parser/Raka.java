package parser;

import parser.test.TestAnnotation;

/**
 * Changes to this file can break JavaFileHandler1Test
 */
public class Raka {

    @TestAnnotation
    private String field1;

    /*@TestAnnotation*/
    private String field2 = "field2";

    private int field3 = 1234, field4  ;

    private java.lang.Boolean  field5      ;



    private String      field10 = "three";

    private java.lang.Boolean      field12=false,  field13;
    protected String surname = null;

    protected int i = 0;


    public String getField1() {
        return field1;
    }

    /**
     * javadoc comment
     * @param field1
     */
    //****hello
    public String getField2() {
        return field2;
    }

    public static final java.lang.String echo(String msg) {
        return msg;
    }

    public String getUserName() {
        return surname;
    }


    //some comments at the end of file 1
}

//some comments at the end of file 2

//end
