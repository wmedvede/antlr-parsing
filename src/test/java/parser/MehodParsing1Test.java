package parser;


import org.junit.Test;
import parser.metadata.*;
import util.ParserUtil;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static parser.ParserAssert.assertEqualsFieldDeclaration;
import static parser.ParserAssert.assertEqualsMethodDeclaration;

public class MehodParsing1Test extends JavaParserBaseTest {

    public MehodParsing1Test() {
        super("MethodParsing1.java");
    }

    @Test
    public void testMethodsSentencesReading() {
        try {
            List<String>methodSentences = new ArrayList<String>();
            methodSentences.add("public String getName() { return name; }");
            methodSentences.add("public void setName(String name) { this.name = name; }");
            methodSentences.add("public String getSurname() { return surname; }");
            methodSentences.add("public void setSurname(String surname) { this.surname = surname; }");
            methodSentences.add("public java.util.List<String> getList() { return null; }");
            methodSentences.add("public java.util.AbstractList<String> getNamesList(final int param1, java.lang.Integer param2, java.util.List<java.lang.Integer> param3) { return null; }");

            parser.compilationUnit();
            List<MethodDeclarationDesc> methods  = parser.getMethods();

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
            List<MethodDeclarationDesc> methodDeclarations = new ArrayList<MethodDeclarationDesc>();

            MethodDeclarationDesc method;

            method = new MethodDeclarationDesc(null, -1, -1);
            method.setName("getName");
            method.addModifier(new ModifierDesc(null, -1, -1, "public"));
            method.setType(new TypeDesc(null, -1, -1));
            method.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDesc("String", -1, -1));
            methodDeclarations.add(method);

            // public void setName(String name) { this.name = name; }
            method = new MethodDeclarationDesc(null, -1, -1);
            method.setName("setName");
            method.addModifier(new ModifierDesc(null, -1, -1, "public"));
            method.setType(null); //returns void
            NormalParameterDeclarationDesc param1 = new NormalParameterDeclarationDesc(null, -1, -1, "name");
            param1.setType(new TypeDesc(null, -1, -1));
            param1.getType().setClassOrInterfaceType(new ClassOrInterfaceTypeDesc("String", -1, -1));
            method.addParameter(param1);
            methodDeclarations.add(method);

            parser.compilationUnit();
            List<MethodDeclarationDesc> methods = parser.getMethods();
            for (int i = 0; i < methodDeclarations.size(); i++) {
                assertEqualsMethodDeclaration(buffer, methodDeclarations.get(i), methods.get(i));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
