package parser.metadata;


import java.util.ArrayList;
import java.util.List;

public class ClassOrInterfaceTypeDesc extends ElementDescriptor {


    List<IdentifierWithTypeArgumentsDesc> identifierWithTypeArguments = new ArrayList<IdentifierWithTypeArgumentsDesc>();


    public ClassOrInterfaceTypeDesc() {
        super(ElementType.CLASS_OR_INTERFACE_TYPE);
    }

    public ClassOrInterfaceTypeDesc(String text, int start, int stop, int line, int position) {
        super(ElementType.CLASS_OR_INTERFACE_TYPE, text, start, stop, line, position);
    }

    public ClassOrInterfaceTypeDesc(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public void addIdentifierWithTypeArguments(IdentifierWithTypeArgumentsDesc identifierWithTypeArgumentsDesc) {
        identifierWithTypeArguments.add(identifierWithTypeArgumentsDesc);
    }

    public String getClassName() {
        return getText();
    }

}
