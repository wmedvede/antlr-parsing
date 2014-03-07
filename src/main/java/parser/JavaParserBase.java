package parser;

import org.antlr.runtime.*;

import java.util.Stack;

import parser.descr.*;
import parser.descr.ElementDescriptor.ElementType;

public class JavaParserBase extends Parser {

    public JavaParserBase(TokenStream input) {
        super(input);
        initContext();
    }

    public JavaParserBase(TokenStream input, RecognizerSharedState state) {
        super(input, state);
        initContext();
    }

    protected FileDescr fileDescr = new FileDescr();

    protected Stack<ElementDescriptor> context = new Stack<ElementDescriptor>();

    protected boolean declaringMethodReturnType = false;

    protected int classLevel = 0;

    public FileDescr getFileDescr() {
        return fileDescr;
    }

    private ClassDescr getClassDesc() {
        return fileDescr.getClassDescr();
    }

    protected void initContext() {
        context.push(fileDescr);
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

    protected boolean isModifierListOnTop() {
        return isOnTop(ElementType.MODIFIER_LIST);
    }

    protected boolean isClassOnTop() {
        return isOnTop(ElementType.CLASS);
    }

    protected boolean isFileOnTop() {
        return isOnTop(ElementType.FILE);
    }

    protected boolean isOnTop(ElementType elementType) {
        return !context.empty() && context.peek().isElementType(elementType);
    }

    protected MethodDescr popMethod() {
        return isMethodOnTop() ? (MethodDescr)context.pop() : null;
    }

    protected MethodDescr peekMethod() {
        return isMethodOnTop() ? (MethodDescr)context.peek() : null;
    }

    protected FieldDescr popField() {
        return isFieldOnTop() ? (FieldDescr)context.pop() : null;
    }

    protected FieldDescr peekField() {
        return isFieldOnTop() ? (FieldDescr)context.peek() : null;
    }

    protected TypeDescr popType() {
        return isTypeOnTop() ? (TypeDescr)context.pop() : null;
    }

    protected TypeDescr peekType() {
        return isTypeOnTop() ? (TypeDescr)context.peek() : null;
    }

    protected ClassOrInterfaceTypeDescr popClassOrInterfaceType() {
        return isClassOrInterfaceTypeOnTop() ? (ClassOrInterfaceTypeDescr)context.pop() : null;
    }

    protected ClassOrInterfaceTypeDescr peekClassOrInterfaceType() {
        return isClassOrInterfaceTypeOnTop() ? (ClassOrInterfaceTypeDescr)context.peek() : null;
    }

    protected TypeArgumentDescr popTypeArgument() {
        return isTypeArgumentOnTop() ? (TypeArgumentDescr)context.pop() : null;
    }

    protected ParameterDescr popParameter() {
        return isParameterOnTop() ? (ParameterDescr)context.pop() : null;
    }

    protected ParameterDescr peekParameter() {
        return isParameterOnTop() ? (ParameterDescr)context.peek() : null;
    }

    protected NormalParameterDescr popNormalParameter() {
        return isNormalParameterOnTop() ? (NormalParameterDescr)context.pop() : null;
    }

    protected NormalParameterDescr peekNormalParameter() {
        return isNormalParameterOnTop() ? (NormalParameterDescr)context.peek() : null;
    }

    protected EllipsisParameterDescr popEllipsisParameter() {
        return isEllipsisParameterOnTop() ? (EllipsisParameterDescr)context.pop() : null;
    }

    protected EllipsisParameterDescr peekEllipsisParameter() {
        return isEllipsisParameterOnTop() ? (EllipsisParameterDescr)context.peek() : null;
    }

    protected ModifierListDescr popModifierList() {
        return isModifierListOnTop() ? (ModifierListDescr)context.pop() : null;
    }

    protected ModifierListDescr peekModifierList() {
        return isModifierListOnTop() ? (ModifierListDescr)context.peek() : null;
    }

    protected ClassDescr popClass() {
        return isClassOnTop() ? (ClassDescr) context.pop() : null;
    }

    protected ClassDescr peekClass() {
        return isClassOnTop() ? (ClassDescr) context.peek() : null;
    }

    protected FileDescr popFile() {
        return isFileOnTop() ? (FileDescr) context.pop() : null;
    }

    protected FileDescr peekFile() {
        return isFileOnTop() ? (FileDescr) context.peek() : null;
    }

    protected TypeArgumentDescr peekTypeArgument() {
        return isTypeArgumentOnTop() ? (TypeArgumentDescr)context.peek() : null;
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

    protected void processType(TypeDescr type) {
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

    protected void processModifiers(ModifierListDescr modifiers) {
        if (isDeclaringMainClass()) {
            if ( isTypeArgumentOnTop() || isMethodOnTop() || isFieldOnTop() || isParameterOnTop() ) {
                peekHasModifiers().setModifiers(modifiers);
            }
        }
    }

    protected void processParameter(ParameterDescr parameterDesc) {
        if (isDeclaringMainClass()) {
            MethodDescr method = peekMethod();
            //TODO check if this control is enough
            if (method != null) {
                method.addParameter(parameterDesc);
            }
        }
    }

    protected void processMethod(MethodDescr methodDesc) {
        if (isDeclaringMainClass()) {
            getClassDesc().addMember(methodDesc);
        }
    }

    protected void setFormalParamsStart(String text, int line, int position) {
        if (isDeclaringMainClass()) {
            if ( isMethodOnTop() ) {
                peekMethod().setParamsStart(new TextTokenElementDescr(text, line, position));
            }
        }
    }

    protected void setFormalParamsStop(String text, int line, int position) {
        if (isDeclaringMainClass()) {
            if ( isMethodOnTop() ) {
                peekMethod().setParamsStop(new TextTokenElementDescr(text, line, position));
            }
        }
    }

    protected void processField(FieldDescr fieldDesc) {
        if (isDeclaringMainClass()) {
            getClassDesc().addMember(fieldDesc);
        }
    }

    protected void processClass(ClassDescr classDescr) {
        if (isDeclaringMainClass()) {
            fileDescr.setClassDescr(classDescr);
            context.push(classDescr);
        }
    }
}