package parser;

import parser.metadata.*;
import util.ParserUtil;

import static org.junit.Assert.assertEquals;
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

    public static void assertEqualsPrimitiveType(StringBuffer buffer, PrimitiveTypeDesc type1, PrimitiveTypeDesc type2) {
        if (type1 != null && type2 != null) {
            assertEquals(type1.getName(), type2.getName());
        }
        if (type1 == null) assertNull(type2);
        if (type2 == null) assertNull(type1);
    }

    public static void assertEqualsClassType(StringBuffer buffer, ClassTypeDesc class1, ClassTypeDesc class2) {
        if (class1 != null && class2 != null) {
            assertEquals(class1.getName(), class2.getName());
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

    public static void assertEqualsVariableDeclarationDesc(StringBuffer buffer, VariableDeclarationDesc var1,  VariableDeclarationDesc var2) {
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
            assertEquals(field1.getModifiers().size(), field2.getModifiers().size());
            for (int i = 0; i < field1.getModifiers().size(); i++) {
                assertEqualsModifier(buffer, field1.getModifiers().get(i), field2.getModifiers().get(i));
            }
            assertEquals(field1.getVariableDeclarations().size(), field2.getVariableDeclarations().size());
            for (int i = 0; i < field1.getVariableDeclarations().size(); i++) {
                assertEqualsVariableDeclarationDesc(buffer, field1.getVariableDeclarations().get(i), field2.getVariableDeclarations().get(i));
            }
            if (field1.getType() instanceof ClassTypeDesc) {
                assertEquals(true, field2.getType() instanceof ClassTypeDesc);
                assertEqualsClassType(buffer, (ClassTypeDesc)field1.getType(), (ClassTypeDesc)field2.getType());
            } else if (field1.getType() instanceof PrimitiveTypeDesc) {
                assertEquals(true, field2.getType() instanceof PrimitiveTypeDesc);
                assertEqualsPrimitiveType(buffer, (PrimitiveTypeDesc)field1.getType(), (PrimitiveTypeDesc)field2.getType());
            } else {
                assertNull(field1.getType());
                assertNull(field2.getType());
            }
        }
        if (field1 == null) assertNull(field2);
        if (field2 == null) assertNull(field1);
    }
}
