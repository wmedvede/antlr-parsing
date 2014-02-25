package util;

import org.antlr.runtime.CommonToken;
import parser.JavaParser;
import parser.metadata.ElementDescriptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class ParserUtil {

    public static String printCommonToken(Object token) {

        String value = "startIndex: " /*+ (token != null ? token.getStartIndex() : null )
                   + ", stopIndex: " + (token != null ? token.getStopIndex() : null )
                   + ", text: " + (token != null ? token.getText() : null)
                   + ", type: " + (token != null ? JavaParser.tokenNames[token.getType()] : null)*/;

        //System.out.println("value = " + value);

        List<String> l = new ArrayList<String>();
        return value;
    }

    public static void printModifiers(List<String> modifiers) {
        for (String modifier : modifiers) {
            System.out.println("--> " + modifier);
        }

    }

    public static String startTokenDeclaration(String token, Object start, Object stop) {

        CommonToken startToken = (CommonToken)start;
        CommonToken endToken = (CommonToken)stop;

        //String value = "[token: " + token + ", startIndex: " + startToken.getStartIndex() + ", stopIndex: " + endToken.getStopIndex() + "]";
        //System.out.println(value);
        return "";//value;
    }

    public static int getStartIndex(CommonToken token) {
        return token != null ? token.getStartIndex() : -1;
    }

    public static int getStopIndex(CommonToken token) {
        return token != null ? token.getStopIndex() : -1;
    }

    public static int getLine(CommonToken token) {
        return token != null ? token.getLine() : -1;
    }

    public static int getPositionInLine(CommonToken token) {
        return token != null ? token.getCharPositionInLine() : -1;

    }

    public static String readElement(StringBuffer stringBuffer, ElementDescriptor elementDescriptor) {
        if (stringBuffer == null || elementDescriptor == null) return null;

            //add more controls later, now I want to see if something fails.
        /*
                elementDescriptor.getStart() < 0 || elementDescriptor.getStop() < 0 ||
                stringBuffer.length() < elementDescriptor.getStart()
        */

        return stringBuffer.substring(elementDescriptor.getStart(), elementDescriptor.getStop());

    }


    public static String readString(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
            out.append('\n');
        }
        reader.close();
        return out.toString();
    }

}
