package parser;


import org.junit.Ignore;
import org.junit.Test;
import parser.descr.*;
import parser.util.ParserUtil;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static parser.ParserAssert.assertEqualsMethodDeclaration;

public class MehodParsing1Test extends JavaParserBaseTest {

    public MehodParsing1Test() {
        super("MethodParsing1.java");
    }

    @Ignore
    public void testMethodsSentencesReading() {
        try {
            List<String>methodSentences = new ArrayList<String>();
            methodSentences.add("public String getField1() { return field1; }");
            methodSentences.add("public void setField1(String field1) { this.field1 = field1; }");
            methodSentences.add("private int method1() { return -1; }");
            methodSentences.add("private void method2() {}");
            methodSentences.add("public static java.lang.String method3() { return null; }");
            methodSentences.add("public static final Integer method4() { return null; }");
            methodSentences.add("public void method5(java.lang.Integer param1, int param2) {}");
            methodSentences.add("java.util.List<java.lang.String> method6() { return null;    }");
            methodSentences.add("protected   java.util.AbstractList<String>    method7  ( final int   param1 ,  java.lang.Integer   param2  ,   java.util.List<java.lang.Integer>      param3      ) {    return  null  ;    }");
            methodSentences.add("int method8  ( final int   param1 ,  java.lang.Integer   param2   ) [  ]   [    ] { return null; }");
            methodSentences.add("int method9 ( final Object ...  param1) { return -1;}");
            methodSentences.add("private java.util.AbstractList<Object> method10  (  final java.lang.String param1,  int param2 , List<java.util.List<String>>...param3) { return null; }");

            assertClass();
            List<MethodDescr> methods  = parser.getFileDescr().getClassDescr().getMethods();
            int i = 0;
            for (String methodSentence : methodSentences) {
                assertEquals(methodSentences.get(i), ParserUtil.readElement(buffer, methods.get(i)));
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMethodDeclarations() {

        try {
            List<MethodDescr> methodDeclarations = new ArrayList<MethodDescr>();

            MethodDescr method;
            NormalParameterDescr param1;
            NormalParameterDescr param2;
            NormalParameterDescr param3;
            EllipsisParameterDescr ellipsisParam;


            //"public String getField1() { return field1; }"
            method = new MethodDescr(null, -1, -1);
            method.setName("getField1");
            method.addModifier(new ModifierDescr(null, -1, -1, "public"));
            method.setType(new TypeDescr(null, -1, -1));
            method.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("String", -1, -1));
            methodDeclarations.add(method);

            //"public void setField1(String field1) { this.field1 = field1; }"
            method = new MethodDescr(null, -1, -1);
            method.setName("setField1");
            method.addModifier(new ModifierDescr(null, -1, -1, "public"));
            method.setType(null); //returns void
            param1 = new NormalParameterDescr(null, -1, -1, "field1");
            param1.setType(new TypeDescr(null, -1, -1));
            param1.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("String", -1, -1));
            method.addParameter(param1);
            methodDeclarations.add(method);

            //"private int method1() { return -1; }"
            method = new MethodDescr(null, -1, -1);
            method.setName("method1");
            method.addModifier(new ModifierDescr(null, -1, -1, "private"));
            method.setType(new TypeDescr(null, -1, -1));
            method.getType().setPrimitiveType(new PrimitiveTypeDescr(null, -1, -1, "int"));
            methodDeclarations.add(method);

            //"private void method2() {}"
            method = new MethodDescr(null, -1, -1);
            method.setName("method2");
            method.addModifier(new ModifierDescr(null, -1, -1, "private"));
            methodDeclarations.add(method);

            //"public static java.lang.String method3() { return null; }"
            method = new MethodDescr(null, -1, -1);
            method.setName("method3");
            method.addModifier(new ModifierDescr(null, -1, -1, "public"));
            method.addModifier(new ModifierDescr(null, -1, -1, "static"));
            method.setType(new TypeDescr(null, -1, -1));
            method.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("java.lang.String", -1, -1));
            methodDeclarations.add(method);

            //"public static final Integer method4() { return null; }"
            method = new MethodDescr(null, -1, -1);
            method.setName("method4");
            method.addModifier(new ModifierDescr(null, -1, -1, "public"));
            method.addModifier(new ModifierDescr(null, -1, -1, "static"));
            method.addModifier(new ModifierDescr(null, -1, -1, "final"));
            method.setType(new TypeDescr(null, -1, -1));
            method.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("Integer", -1, -1));
            methodDeclarations.add(method);

            //"public void method5(java.lang.Integer param1, int param2) {}"
            method = new MethodDescr(null, -1, -1);
            method.setName("method5");
            method.addModifier(new ModifierDescr(null, -1, -1, "public"));
            method.setType(null); //returns void
            param1 = new NormalParameterDescr(null, -1, -1, "param1");
            param1.setType(new TypeDescr(null, -1, -1));
            param1.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("java.lang.Integer", -1, -1));
            method.addParameter(param1);
            param2 = new NormalParameterDescr(null, -1, -1, "param2");
            param2.setType(new TypeDescr(null, -1, -1));
            param2.getType().setPrimitiveType(new PrimitiveTypeDescr(null, -1, -1, "int"));
            method.addParameter(param2);
            methodDeclarations.add(method);

            //"java.util.List<java.lang.String> method6() { return null;    }"
            method = new MethodDescr(null, -1, -1);
            method.setName("method6");
            method.setType(new TypeDescr(null, -1, -1));
            method.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("java.util.List<java.lang.String>", -1, -1));
            methodDeclarations.add(method);

            //"protected   java.util.AbstractList<String>    method7  ( final int   param1 ,  java.lang.Integer   param2  ,   java.util.List<java.lang.Integer>      param3      ) {    return  null  ;    }"
            method = new MethodDescr(null, -1, -1);
            method.setName("method7");
            method.addModifier(new ModifierDescr(null, -1, -1, "protected"));
            method.setType(new TypeDescr(null, -1, -1));
            method.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("java.util.AbstractList<String>", -1, -1));

            param1 = new NormalParameterDescr(null, -1, -1, "param1");
            param1.setType(new TypeDescr(null, -1, -1));
            param1.getType().setPrimitiveType(new PrimitiveTypeDescr(null, -1, -1, "int"));
            param1.addModifier(new ModifierDescr(null, -1, -1, "final"));
            method.addParameter(param1);

            param2 = new NormalParameterDescr(null, -1, -1, "param2");
            param2.setType(new TypeDescr(null, -1, -1));
            param2.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("java.lang.Integer", -1, -1));
            method.addParameter(param2);

            param3 = new NormalParameterDescr(null, -1, -1, "param3");
            param3.setType(new TypeDescr(null, -1, -1));
            param3.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("java.util.List<java.lang.Integer>", -1, -1));
            method.addParameter(param3);
            methodDeclarations.add(method);

            //"int method8  ( final int   param1 ,  java.lang.Integer   param2)[][] { return null; }"
            method = new MethodDescr(null, -1, -1);
            method.setName("method8");
            method.setType(new TypeDescr(null, -1, -1));
            method.getType().setPrimitiveType(new PrimitiveTypeDescr(null, -1, -1, "int"));

            param1 = new NormalParameterDescr(null, -1, -1, "param1");
            param1.setType(new TypeDescr(null, -1, -1));
            param1.getType().setPrimitiveType(new PrimitiveTypeDescr(null, -1, -1, "int"));
            param1.addModifier(new ModifierDescr(null, -1, -1, "final"));
            method.addParameter(param1);

            param2 = new NormalParameterDescr(null, -1, -1, "param2");
            param2.setType(new TypeDescr(null, -1, -1));
            param2.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("java.lang.Integer", -1, -1));
            method.addParameter(param2);
            method.addDimension(new DimensionDescr("[", -1, -1, "]", -1, -1));
            method.addDimension(new DimensionDescr("[", -1, -1, "]", -1, -1));
            methodDeclarations.add(method);


            //"int method9 ( final Object ...  param1) { return -1;}"
            method = new MethodDescr(null, -1, -1);
            method.setName("method9");
            method.setType(new TypeDescr(null, -1, -1));
            method.getType().setPrimitiveType(new PrimitiveTypeDescr(null, -1, -1, "int"));

            ellipsisParam = new EllipsisParameterDescr(null, -1, -1, "param1");
            ellipsisParam.setType(new TypeDescr(null, -1, -1));
            ellipsisParam.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("Object", -1, -1));
            ellipsisParam.addModifier(new ModifierDescr(null, -1, -1, "final"));
            method.addParameter(ellipsisParam);
            methodDeclarations.add(method);

            //private java.util.AbstractList<Object> method10  (  final java.lang.String param1,  int param2 , List<java.util.List<String>>...param3) { return null; }
            method = new MethodDescr(null, -1, -1);
            method.setName("method10");
            method.setType(new TypeDescr(null, -1, -1));
            method.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("java.util.AbstractList<Object>", -1, -1));
            method.addModifier(new ModifierDescr(null, -1, -1, "private"));

            param1 = new NormalParameterDescr(null, -1, -1, "param1");
            param1.setType(new TypeDescr(null, -1, -1));
            param1.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("java.lang.String", -1 , -1));
            param1.addModifier(new ModifierDescr(null, -1, -1, "final"));
            method.addParameter(param1);

            param2 = new NormalParameterDescr(null, -1, -1, "param2");
            param2.setType(new TypeDescr(null, -1, -1));
            param2.getType().setPrimitiveType(new PrimitiveTypeDescr(null, -1, -1, "int"));
            method.addParameter(param2);

            ellipsisParam = new EllipsisParameterDescr(null, -1, -1, "param3");
            ellipsisParam.setType(new TypeDescr(null, -1, -1));
            ellipsisParam.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDescr("List<java.util.List<String>>", -1, -1));
            method.addParameter(ellipsisParam);
            methodDeclarations.add(method);

            assertClass();

            List<MethodDescr> methods = parser.getFileDescr().getClassDescr().getMethods();
            assertEquals(methodDeclarations.size(), methods.size());
            for (int i = 0; i < methodDeclarations.size(); i++) {
                assertEqualsMethodDeclaration(buffer, methodDeclarations.get(i), methods.get(i));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*


        public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }

    public void setSurname(String surname) { this.surname = surname; }

    public java.util.List<String> getList() { return null; }

    public java.util.AbstractList<String> getNamesList(final int param1, java.lang.Integer param2, java.util.List<java.lang.Integer> param3) { return null; }



     */
}
