package parser;

import parser.metadata.*;
import util.ParserUtil;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ParserAssert {

    public static void assertEqualsModifier(StringBuffer buffer, final ModifierDesc mod1, final ModifierDesc mod2) {
        if (mod1 != null && mod2 != null) {
            assertEquals(mod1.getName(), mod2.getName());
            //TODO add this comparation assertEquals(mod1.getName(), ParserUtil.readElement(buffer, mod2));
        }
        if (mod1 == null) assertNull(mod2);
        if (mod2 == null) assertNull(mod1);
    }

    public static void assertEqualsModifiers(StringBuffer buffer, List<ModifierDesc> modifiers1, List<ModifierDesc> modifiers2) {
        if (modifiers1 != null && modifiers2 != null) {
            assertEquals(modifiers1.size(), modifiers2.size());
            for (int i = 0; i < modifiers1.size(); i++) {
                assertEqualsModifier(buffer, modifiers1.get(i), modifiers2.get(i));
            }
        }
        if (modifiers1 == null) assertNull(modifiers2);
        if (modifiers2 == null) assertNull(modifiers1);
    }

    public static void assertEqualsPrimitiveType(StringBuffer buffer, PrimitiveTypeDesc type1, PrimitiveTypeDesc type2) {
        if (type1 != null && type2 != null) {
            assertEquals(type1.getName(), type2.getName());
        }
        if (type1 == null) assertNull(type2);
        if (type2 == null) assertNull(type1);
    }

    public static void assertEqualsClassType(StringBuffer buffer, ClassOrInterfaceTypeDesc class1, ClassOrInterfaceTypeDesc class2) {
        if (class1 != null && class2 != null) {
            assertEquals(class1.getClassName(), class2.getClassName());
        }
        if (class1 == null) assertNull(class2);
        if (class2 == null) assertNull(class1);
    }

    public static void assertEqualsVariableInitializer(StringBuffer buffer, VariableInitializerDesc var1,  VariableInitializerDesc var2) {
        if (var1 != null && var2 != null) {
            assertEquals(var1.getInitializerExpr(), ParserUtil.readElement(buffer, var2));
        }
        if (var1 == null) assertNull(var2);
        if (var2 == null) assertNull(var1);
    }

    public static void assertEqualsVariableDeclaration(StringBuffer buffer, VariableDeclarationDesc var1,  VariableDeclarationDesc var2) {
        if (var1 != null && var2 != null) {
            assertEquals(var1.getIdentifier(), var2.getIdentifier());
            assertEquals(var1.getDimensions(), var2.getDimensions());
            assertEqualsVariableInitializer(buffer, var1.getVariableInitializer(), var2.getVariableInitializer());
        }
        if (var1 == null) assertNull(var2);
        if (var2 == null) assertNull(var1);
    }

    public static void assertEqualsFieldDeclaration(StringBuffer buffer, FieldDeclarationDesc field1, FieldDeclarationDesc field2) {
        if (field1 != null && field2 != null) {
            assertEqualsModifiers(buffer, field1.getModifiers(), field2.getModifiers());
            assertEquals(field1.getVariableDeclarations().size(), field2.getVariableDeclarations().size());
            for (int i = 0; i < field1.getVariableDeclarations().size(); i++) {
                assertEqualsVariableDeclaration(buffer, field1.getVariableDeclarations().get(i), field2.getVariableDeclarations().get(i));
            }
            assertEqualsType(buffer, field1.getType(), field2.getType());
        }
        if (field1 == null) assertNull(field2);
        if (field2 == null) assertNull(field1);
    }

    public static void assertEqualsType(StringBuffer buffer, TypeDesc type1, TypeDesc type2) {

        if (type1 != null && type2 != null) {
            if (type1.isClassOrInterfaceType()) {
                assertEquals(true, type2.isClassOrInterfaceType());
                assertEqualsClassType(buffer, type1.getClassOrInterfaceType(), type2.getClassOrInterfaceType());
            } else if (type1.isPrimitiveType()) {
                assertEquals(true, type2.isPrimitiveType());
                assertEqualsPrimitiveType(buffer, type1.getPrimitiveType(), type2.getPrimitiveType());
            } else {
                assertNull(type1);
                assertNull(type2);
            }
        }
        if (type1 == null) assertNull(type2);
        if (type2 == null) assertNull(type1);
    }

    public static void assertEqualsMethodDeclaration(StringBuffer buffer, MethodDeclarationDesc method1, MethodDeclarationDesc method2) {
        if (method1 != null && method2 != null) {
            assertEqualsModifiers(buffer, method1.getModifiers(), method2.getModifiers());
            assertEqualsType(buffer, method1.getType(), method2.getType());
            assertEquals(method1.getParameters().size(), method2.getParameters().size());
            for (int i = 0; i < method1.getParameters().size(); i++) {
                assertEqualsParameter(buffer, method1.getParameters().get(i), method2.getParameters().get(i));
            }
        }
        if (method1 == null) assertNull(method2);
        if (method2 == null) assertNotNull(method1);
    }

    public static void assertEqualsParameter(StringBuffer buffer, ParameterDeclarationDesc param1, ParameterDeclarationDesc param2) {
        if (param1 != null && param2 != null) {
            assertEquals(param1.getName(), param2.getName());
            assertEqualsType(buffer, param1.getType(), param2.getType());
            assertEqualsModifiers(buffer, param1.getModifiers(), param2.getModifiers());
        }
        if (param1 == null) assertNull(param2);
        if (param2 == null) assertNull(param1);
    }

}
