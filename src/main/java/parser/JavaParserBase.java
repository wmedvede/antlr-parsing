package parser;

import org.antlr.runtime.*;

import java.util.Stack;
import java.util.ArrayList;
import java.util.List;

import parser.metadata.*;
import parser.metadata.ElementDescriptor.ElementType;

public class JavaParserBase extends Parser {

    public JavaParserBase(TokenStream input) {
        super(input);
    }

    public JavaParserBase(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

    protected Stack<ElementDescriptor> context = new Stack<ElementDescriptor>();

    protected List<FieldDeclarationDesc> fields = new ArrayList<FieldDeclarationDesc>();

    protected List<MethodDeclarationDesc> methods = new ArrayList<MethodDeclarationDesc>();

    protected boolean declaringMethodReturnType = false;

    protected int classLevel = 0;

    public List<FieldDeclarationDesc> getFields() {
        return fields;
    }

    public List<MethodDeclarationDesc> getMethods() {
        return methods;
    }

    protected void log(String message) {
        //TODO setup log stuff
        System.out.println(message + " : " + new java.util.Date());
    }

    protected boolean isFieldOnTop() {
        return isOnTop(ElementType.FIELD);
    }

    protected boolean isMethodOnTop() {
        return isOnTop(ElementType.METHOD);
    }

    protected boolean isTypeOnTop() {
        return isOnTop(ElementType.TYPE);
    }

    protected boolean isClassOrInterfaceTypeOnTop() {
        return isOnTop(ElementType.CLASS_OR_INTERFACE_TYPE);
    }

    protected boolean isTypeArgumentOnTop() {
        return isOnTop(ElementType.TYPE_ARGUMENT);
    }

    protected boolean isParameterOnTop() {
        return isOnTop(ElementType.NORMAL_PARAMETER) || isOnTop(ElementType.ELLIPSIS_PARAMETER);
    }

    protected boolean isNormalParameterOnTop() {
        return isOnTop(ElementType.NORMAL_PARAMETER);
    }

    protected boolean isEllipsisParameterOnTop() {
        return isOnTop(ElementType.ELLIPSIS_PARAMETER);
    }

    protected boolean isOnTop(ElementType elementType) {
        return !context.empty() && context.peek().isElementType(elementType);
    }

    protected MethodDeclarationDesc popMethod() {
        return isMethodOnTop() ? (MethodDeclarationDesc)context.pop() : null;
    }

    protected MethodDeclarationDesc peekMethod() {
        return isMethodOnTop() ? (MethodDeclarationDesc)context.peek() : null;
    }

    protected FieldDeclarationDesc popField() {
        return isFieldOnTop() ? (FieldDeclarationDesc)context.pop() : null;
    }

    protected FieldDeclarationDesc peekField() {
        return isFieldOnTop() ? (FieldDeclarationDesc)context.peek() : null;
    }

    protected TypeDesc popType() {
        return isTypeOnTop() ? (TypeDesc)context.pop() : null;
    }

    protected TypeDesc peekType() {
        return isTypeOnTop() ? (TypeDesc)context.peek() : null;
    }

    protected ClassOrInterfaceTypeDesc popClassOrInterfaceType() {
        return isClassOrInterfaceTypeOnTop() ? (ClassOrInterfaceTypeDesc)context.pop() : null;
    }

    protected ClassOrInterfaceTypeDesc peekClassOrInterfaceType() {
        return isClassOrInterfaceTypeOnTop() ? (ClassOrInterfaceTypeDesc)context.peek() : null;
    }

    protected TypeArgumentDesc popTypeArgument() {
        return isTypeArgumentOnTop() ? (TypeArgumentDesc)context.pop() : null;
    }

    protected ParameterDeclarationDesc popParameter() {
        return isParameterOnTop() ? (ParameterDeclarationDesc)context.pop() : null;
    }

    protected ParameterDeclarationDesc peekParameter() {
        return isParameterOnTop() ? (ParameterDeclarationDesc)context.peek() : null;
    }

    protected NormalParameterDeclarationDesc popNormalParameter() {
        return isNormalParameterOnTop() ? (NormalParameterDeclarationDesc)context.pop() : null;
    }

    protected NormalParameterDeclarationDesc peekNormalParameter() {
        return isNormalParameterOnTop() ? (NormalParameterDeclarationDesc)context.peek() : null;
    }

    protected EllipsisParameterDeclarationDesc popEllipsisParameter() {
        return isEllipsisParameterOnTop() ? (EllipsisParameterDeclarationDesc)context.pop() : null;
    }

    protected EllipsisParameterDeclarationDesc peekEllipsisParameter() {
        return isEllipsisParameterOnTop() ? (EllipsisParameterDeclarationDesc)context.peek() : null;
    }

    protected TypeArgumentDesc peekTypeArgument() {
        return isTypeArgumentOnTop() ? (TypeArgumentDesc)context.peek() : null;
    }

    protected HasModifiers peekHasModifiers() {
        return !context.empty() && (context.peek() instanceof HasModifiers) ? (HasModifiers)context.peek() : null;
    }

    protected HasType peekHasType() {
        return !context.empty() && (context.peek() instanceof HasType) ? (HasType)context.peek() : null;
    }

    protected HasClassOrInterfaceType peekHasClassOrInterfaceType() {
        return !context.empty() && (context.peek() instanceof HasClassOrInterfaceType) ? (HasClassOrInterfaceType)context.peek() : null;
    }

    protected HasPrimitiveType peekHasPrimitiveType() {
        return !context.empty() && (context.peek() instanceof HasPrimitiveType) ? (HasPrimitiveType)context.peek() : null;
    }

    protected HasTypeArguments peekHasTypeArguments() {
        return !context.empty() && (context.peek() instanceof HasTypeArguments) ? (HasTypeArguments)context.peek() : null;
    }

    protected int start(CommonToken token) {
        return token != null ? token.getStartIndex() : -1;
    }

    protected int stop(CommonToken token) {
        return token != null ? token.getStopIndex() : -1;
    }

    protected int line(Token token) {
        return token != null ? token.getLine() : -1;
    }

    protected int position(Token token) {
        return token != null ? token.getCharPositionInLine() : -1;
    }

    protected boolean isBacktracking() {
        return state.backtracking > 0;
    }

    protected void updateOnAfter(ElementDescriptor element, String text, CommonToken stop) {
        element.setText(text);
        element.setStop(stop(stop));
    }

    protected boolean isDeclaringMethodReturnType() {
        return declaringMethodReturnType;
    }

    protected void setDeclaringMethodReturnType(boolean declaringMethodReturnType) {
        this.declaringMethodReturnType = declaringMethodReturnType;
    }

    public boolean isDeclaringMainClass() {
        return classLevel == 1;
    }

    public int increaseClassLevel() {
        return ++classLevel;
    }

    public int decreaseClassLevel() {
        return --classLevel;
    }

    protected void processType(TypeDesc type) {
        //if we are processing a method declaration return type, or a method parameter, or a field type
        if (isDeclaringMainClass()) {
            if (isTypeArgumentOnTop()) {
                peekTypeArgument().setType(type);
            } else if (isFieldOnTop()) {
                peekField().setType(type);
            } else if (isMethodOnTop() && declaringMethodReturnType) {
                peekMethod().setType(type);
            } else if (isParameterOnTop()) {
                peekParameter().setType(type);
            }
        }
    }

    protected void processModifier(ModifierDesc modifierDesc) {
        if (isDeclaringMainClass()) {
            if ( isTypeArgumentOnTop() || isMethodOnTop() || isFieldOnTop() || isParameterOnTop() ) {
                peekHasModifiers().addModifier(modifierDesc);
            }
        }
    }

    protected void processParameter(ParameterDeclarationDesc parameterDesc) {
        if (isDeclaringMainClass()) {
            MethodDeclarationDesc method = peekMethod();
            //TODO check if this control is enough
            if (method != null) {
                method.addParameter(parameterDesc);
            }
        }
    }

    protected void processMethod(MethodDeclarationDesc methodDesc) {
        if (isDeclaringMainClass()) {
            methods.add(methodDesc);
        }
    }

    protected void setFormalParamsStart(String text, int line, int position) {
        if (isDeclaringMainClass()) {
            if ( isMethodOnTop() ) {
                peekMethod().setParamsStart(new TextTokenElementDescriptor(text, line, position));
            }
        }
    }

    protected void setFormalParamsStop(String text, int line, int position) {
        if (isDeclaringMainClass()) {
            if ( isMethodOnTop() ) {
                peekMethod().setParamsStop(new TextTokenElementDescriptor(text, line, position));
            }
        }
    }

    protected void processField(FieldDeclarationDesc fieldDesc) {
        if (isDeclaringMainClass()) {
            fields.add(fieldDesc);
        }
    }
}