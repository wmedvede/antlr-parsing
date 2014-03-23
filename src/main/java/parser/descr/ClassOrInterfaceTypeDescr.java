package parser.descr;


import java.util.ArrayList;
import java.util.List;

public class ClassOrInterfaceTypeDescr extends ElementDescriptor {


    public ClassOrInterfaceTypeDescr() {
        super(ElementType.CLASS_OR_INTERFACE_TYPE);
    }

    public ClassOrInterfaceTypeDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.CLASS_OR_INTERFACE_TYPE, text, start, stop, line, position);
    }

    public ClassOrInterfaceTypeDescr(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public void addIdentifierWithTypeArgument(IdentifierWithTypeArgumentsDescr identifierWithTypeArgumentsDescr) {
        getElements().add(identifierWithTypeArgumentsDescr);
    }

    public List<IdentifierWithTypeArgumentsDescr> getIdentifierWithTypeArguments() {
        List<IdentifierWithTypeArgumentsDescr> identifiers = new ArrayList<IdentifierWithTypeArgumentsDescr>();
        for (ElementDescriptor member :  getElements().getElementsByType(ElementType.IDENTIFIER_WITH_TYPE_ARGUMENTS)) {
            identifiers.add((IdentifierWithTypeArgumentsDescr)member);
        }
        return identifiers;
    }

    public String getClassName() {
        return getText();
    }

}
