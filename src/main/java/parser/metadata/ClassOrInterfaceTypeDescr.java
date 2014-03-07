package parser.metadata;


import java.util.ArrayList;
import java.util.List;

public class ClassOrInterfaceTypeDescr extends ElementDescriptor {


    List<IdentifierWithTypeArgumentsDescr> identifierWithTypeArguments = new ArrayList<IdentifierWithTypeArgumentsDescr>();


    public ClassOrInterfaceTypeDescr() {
        super(ElementType.CLASS_OR_INTERFACE_TYPE);
    }

    public ClassOrInterfaceTypeDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.CLASS_OR_INTERFACE_TYPE, text, start, stop, line, position);
    }

    public ClassOrInterfaceTypeDescr(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public void addIdentifierWithTypeArguments(IdentifierWithTypeArgumentsDescr identifierWithTypeArgumentsDescr) {
        identifierWithTypeArguments.add(identifierWithTypeArgumentsDescr);
    }

    public String getClassName() {
        return getText();
    }

}
