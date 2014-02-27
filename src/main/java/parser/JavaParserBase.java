package parser;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.TokenStream;


import java.util.Stack;
import java.util.ArrayList;
import java.util.List;

import util.ParserUtil;
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

    protected TypeDesc currentTypeDesc;

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

    protected HasModifiers peekHasModifiers() {
        return !context.empty() && (context.peek() instanceof HasModifiers) ? (HasModifiers)context.peek() : null;
    }

    protected int start(CommonToken token) {
        return ParserUtil.getStartIndex(token);
    }

    protected int stop(CommonToken token) {
        return ParserUtil.getStopIndex(token);
    }

    protected int line(CommonToken token) {
        return ParserUtil.getLine(token);
    }

    protected int position(CommonToken token) {
        return ParserUtil.getPositionInLine(token);
    }

    protected boolean isBacktracking() {
        return state.backtracking > 0;
    }

}
