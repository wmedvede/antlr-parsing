package parser;

import parser.test.TestAnnotation;

/**
 * Changes to this file can break JavaFileHandler1Test
 */
public class JavaFileHandler1 {

    @TestAnnotation
    private String field1;

    /*@TestAnnotation*/
    private String field2 = "field2";

    private int field3 = 1234, field4  ;

    private java.lang.Boolean  field5 ,    field6 = new Boolean(true) ;

    private String[] field7 = new String[]{"one", "two"}    ;

    private String field8  , field9  ,field10 = "three";

    private java.lang.Boolean field11 = (10 > (15 -4))    , field12=false,  field13;

    public String getField1() {
        return field1;
    }

    /**
     * javadoc comment
     * @param field1
     */
    public void setField1(String field1) {
        this.field1 = field1;  /****** asfsd */
    }//****hello
    public String getField2() {
        return field2;
    }
    public void setField2(String field2) {
        this.field2 = field2;
    }

    //some comments at the end of file 1
}

//some comments at the end of file 2

//end