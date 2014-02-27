/* changes to this file can break FieldsParsing1Test */

package parser;

import java.util.ArrayList;
import java.util.List;

public class FieldParsing1 {


    public String field1;

    public static String field2 ;

    public static final Integer FIELD3 = new Integer("3")  ;

    transient boolean field4;

    protected   List<String>   field5;

        //protected   static List<List<String>> field6 = new ArrayList<List<String>>();

        protected   static List<List<String>> field6 = null;


    public    String[]      field7    ;

    //public    static    java.lang.String   field8[]  =  new String[] {"value1",  "value2" } ;

    public    static    java.lang.String   field8[];

        private    static   String  field9 [][][];

     //protected List<String>[] field10 = new  List[] {  new ArrayList<String>(), new ArrayList<String>() };

    protected List<String>[] field10 = null;

protected int field11    =   11   ;

        protected char field12 = 12,    field13  =  13 ;

    Boolean field14 =   false, field15=true, field16 = !true ;


}
