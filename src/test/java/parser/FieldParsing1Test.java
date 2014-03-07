package parser;

import org.junit.Test;
import parser.descr.*;
import parser.util.ParserUtil;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static parser.ParserAssert.assertEqualsFieldDeclaration;

public class FieldParsing1Test extends JavaParserBaseTest {

    public FieldParsing1Test() {
        super("FieldParsing1.java");
    }

    @Test
    public void testFieldSentencesReading() {
        try {

            List<String> fieldSentences = new ArrayList<String>();
            fieldSentences.add("public String field1;");
            fieldSentences.add("public static String field2 ;");
            fieldSentences.add("public static final Integer FIELD3 = new Integer(\"3\")  ;");
            fieldSentences.add("transient boolean field4;");
            fieldSentences.add("protected   List<String>   field5;");
            //complex initializations cannot be regonized yet
            //fieldSentences.add("protected   static List<List<String>> field6 = new ArrayList<List<String>>();");
            fieldSentences.add("protected   static List<List<String>> field6 = null;");
            fieldSentences.add("public    String[]      field7    ;");
            //fieldSentences.add("public    static    java.lang.String   field8[]  =  new String[] {\"value1\",  \"value2\" } ;");
            fieldSentences.add("public    static    java.lang.String   field8[];");
            fieldSentences.add("private    static   String  field9 [][][];");
            //fieldSentences.add("protected List<String>[] field10 = new  List[] {  new ArrayList<String>(), new ArrayList<String>() };");
            fieldSentences.add("protected List<String>[] field10 = null;");
            fieldSentences.add("protected int field11    =   11   ;");
            fieldSentences.add("protected char field12 = 12,    field13  =  13 ;");
            fieldSentences.add("Boolean field14 =   false, field15=true, field16 = !true ;");

            assertClass();
            List<FieldDescr> fields = parser.getFileDescr().getClassDescr().getFields();
            assertEquals(fieldSentences.size(), fields.size());

            //test field definition sentences
            int i = 0;
            for (String fieldSentence : fieldSentences) {
                assertEquals(fieldSentences.get(i), ParserUtil.readElement(buffer, fields.get(i)));
                i++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFieldDeclarations() {

        try {

            List<FieldDescr> fieldDeclarations = new ArrayList<FieldDescr>();

            FieldDescr fieldDeclaration = new FieldDescr();
            VariableDeclarationDescr var;
            TypeDescr type;


            //field1
            fieldDeclaration.addModifier(new ModifierDescr(null, -1, -1, "public"));
            fieldDeclaration.setType(new TypeDescr(null, -1, -1));
            fieldDeclaration.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("String", -1, -1));
            fieldDeclaration.addVariableDeclaration(new VariableDeclarationDescr(null, -1, -1, "field1"));
            fieldDeclarations.add(fieldDeclaration);

            //field2
            fieldDeclaration = new FieldDescr();
            fieldDeclaration.addModifier(new ModifierDescr(null, -1, -1, "public"));
            fieldDeclaration.addModifier(new ModifierDescr(null, -1, -1, "static"));
            fieldDeclaration.setType(new TypeDescr(null, -1, -1));
            fieldDeclaration.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("String", -1, -1));
            fieldDeclaration.addVariableDeclaration(new VariableDeclarationDescr(null, -1, -1, "field2"));
            fieldDeclarations.add(fieldDeclaration);

            //field3
            fieldDeclaration = new FieldDescr();
            fieldDeclaration.addModifier(new ModifierDescr(null, -1, -1, "public"));
            fieldDeclaration.addModifier(new ModifierDescr(null, -1, -1, "static"));
            fieldDeclaration.addModifier(new ModifierDescr(null, -1, -1, "final"));
            fieldDeclaration.setType(new TypeDescr(null, -1, -1));
            fieldDeclaration.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("Integer", -1, -1));
            fieldDeclaration.addVariableDeclaration(new VariableDeclarationDescr(null, -1, -1, "FIELD3", new VariableInitializerDescr(null, -1, -1, "new Integer(\"3\")")));
            fieldDeclarations.add(fieldDeclaration);

            //field4
            //"transient boolean field4;"
            fieldDeclaration = new FieldDescr();
            fieldDeclaration.addModifier(new ModifierDescr(null, -1, -1, "transient"));
            fieldDeclaration.setType(new TypeDescr(null, -1, -1));
            fieldDeclaration.getType().setPrimitiveType(new PrimitiveTypeDescr(null, -1, -1, "boolean"));
            fieldDeclaration.addVariableDeclaration(new VariableDeclarationDescr(null, -1, -1, "field4"));
            fieldDeclarations.add(fieldDeclaration);

            //field5
            //"protected   List<String>   field5;"
            fieldDeclaration = new FieldDescr();
            fieldDeclaration.addModifier(new ModifierDescr(null, -1, -1, "protected"));
            fieldDeclaration.setType(new TypeDescr(null, -1,-1));
            fieldDeclaration.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("List<String>", -1, -1));
            fieldDeclaration.addVariableDeclaration(new VariableDeclarationDescr(null, -1, -1, "field5"));
            fieldDeclarations.add(fieldDeclaration);

            //field6
            //"protected   static List<List<String>> field6 = new ArrayList<List<String>>();"
            fieldDeclaration = new FieldDescr();
            fieldDeclaration.addModifier(new ModifierDescr(null, -1, -1, "protected"));
            fieldDeclaration.addModifier(new ModifierDescr(null, -1, -1, "static"));
            fieldDeclaration.setType(new TypeDescr(null, -1, -1));
            fieldDeclaration.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("List<List<String>>", -1, -1));
            //fieldDeclaration.addVariableDeclaration(new VariableDeclarationDescr(null, -1, -1, "field6", new VariableInitializerDescr(null, -1, -1, "new ArrayList<List<String>>()")));
            fieldDeclaration.addVariableDeclaration(new VariableDeclarationDescr(null, -1, -1, "field6", new VariableInitializerDescr(null, -1, -1, "null")));
            fieldDeclarations.add(fieldDeclaration);

            //field7
            //"public    String[]      field7    ;"
            fieldDeclaration = new FieldDescr();
            fieldDeclaration.addModifier(new ModifierDescr(null, -1, -1, "public"));
            fieldDeclaration.setType(new TypeDescr(null, -1, -1));
            fieldDeclaration.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("String", -1, -1));
            fieldDeclaration.getType().addDimension(new DimensionDescr());
            fieldDeclaration.addVariableDeclaration(new VariableDeclarationDescr(null, -1, -1, "field7", null));
            fieldDeclarations.add(fieldDeclaration);

            //field8
            //"public    static    java.lang.String   field8[];"
            fieldDeclaration = new FieldDescr();
            fieldDeclaration.addModifier(new ModifierDescr(null, -1, -1, "public"));
            fieldDeclaration.addModifier(new ModifierDescr(null, -1, -1, "static"));
            fieldDeclaration.setType(new TypeDescr(null, -1, -1));
            fieldDeclaration.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("java.lang.String", -1, -1));
            var = new VariableDeclarationDescr(null, -1, -1, "field8", null);
            var.addDimension(new DimensionDescr());
            fieldDeclaration.addVariableDeclaration(var);
            fieldDeclarations.add(fieldDeclaration);

            //field9
            //fieldSentences.add("private    static   String  field9 [][][];");
            fieldDeclaration = new FieldDescr();
            fieldDeclaration.addModifier(new ModifierDescr(null, -1, -1, "private"));
            fieldDeclaration.addModifier(new ModifierDescr(null, -1, -1, "static"));
            fieldDeclaration.setType(new TypeDescr(null, -1, -1));
            fieldDeclaration.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("String", -1, -1));
            var = new VariableDeclarationDescr(null, -1, -1, "field9", null);
            var.addDimension(new DimensionDescr());
            var.addDimension(new DimensionDescr());
            var.addDimension(new DimensionDescr());
            fieldDeclaration.addVariableDeclaration(var);
            fieldDeclarations.add(fieldDeclaration);

            //field10
            // "protected List<String>[] field10 = null;"
            fieldDeclaration = new FieldDescr();
            fieldDeclaration.addModifier(new ModifierDescr(null, -1, -1, "protected"));
            fieldDeclaration.setType(new TypeDescr(null, -1, -1));
            fieldDeclaration.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("List<String>", -1, -1));
            fieldDeclaration.getType().addDimension(new DimensionDescr());
            var = new VariableDeclarationDescr(null, -1, -1, "field10", new VariableInitializerDescr(null, -1, -1, "null"));
            fieldDeclaration.addVariableDeclaration(var);
            fieldDeclarations.add(fieldDeclaration);

            //field11
            // "protected int field11    =   11   ;"
            fieldDeclaration = new FieldDescr();
            fieldDeclaration.addModifier(new ModifierDescr(null, -1, -1, "protected"));
            fieldDeclaration.setType(new TypeDescr(null, -1, -1));
            fieldDeclaration.getType().setPrimitiveType(new PrimitiveTypeDescr(null, -1, -1, "int"));
            var = new VariableDeclarationDescr(null, -1, -1, "field11", new VariableInitializerDescr(null, -1, -1, "11"));
            fieldDeclaration.addVariableDeclaration(var);
            fieldDeclarations.add(fieldDeclaration);

            //field 12, and 13
            //"protected char field12 = 12,    field13  =  13 ;"
            fieldDeclaration = new FieldDescr();
            fieldDeclaration.addModifier(new ModifierDescr(null, -1, -1, "protected"));
            fieldDeclaration.setType(new TypeDescr(null, -1, -1));
            fieldDeclaration.getType().setPrimitiveType(new PrimitiveTypeDescr(null, -1, -1, "char"));
            var = new VariableDeclarationDescr(null, -1, -1, "field12", new VariableInitializerDescr(null, -1, -1, "12"));
            fieldDeclaration.addVariableDeclaration(var);
            var = new VariableDeclarationDescr(null, -1, -1, "field13", new VariableInitializerDescr(null, -1, -1, "13"));
            fieldDeclaration.addVariableDeclaration(var);
            fieldDeclarations.add(fieldDeclaration);


            //field 14, 15, 16
            //"Boolean field14 =   false, field15=true, field16 = !true ;"

            fieldDeclaration = new FieldDescr();
            fieldDeclaration.setType(new TypeDescr(null, -1, -1));
            fieldDeclaration.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("Boolean", -1, -1));
            var = new VariableDeclarationDescr(null, -1, -1, "field14", new VariableInitializerDescr(null, -1, -1, "false"));
            fieldDeclaration.addVariableDeclaration(var);
            var = new VariableDeclarationDescr(null, -1, -1, "field15", new VariableInitializerDescr(null, -1, -1, "true"));
            fieldDeclaration.addVariableDeclaration(var);
            var = new VariableDeclarationDescr(null, -1, -1, "field16", new VariableInitializerDescr(null, -1, -1, "!true"));
            fieldDeclaration.addVariableDeclaration(var);
            fieldDeclarations.add(fieldDeclaration);

            assertClass();
            List<FieldDescr> fields = parser.getFileDescr().getClassDescr().getFields();
            for (int i = 0; i < fieldDeclarations.size(); i++) {
                assertEqualsFieldDeclaration(buffer, fieldDeclarations.get(i), fields.get(i));
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
