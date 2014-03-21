package parser.descr;


import java.util.ArrayList;
import java.util.List;

public class QualifiedNameDescr extends ElementDescriptor {

    private List<TextTokenElementDescr> elements = new ArrayList<TextTokenElementDescr>();

    public QualifiedNameDescr() {
        super(ElementType.QUALIFIED_NAME);
    }

    public QualifiedNameDescr(String text, int start, int line, int position) {
        this(text, start, -1, line, position);
    }

    public QualifiedNameDescr(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public QualifiedNameDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.QUALIFIED_NAME, text, start, stop, line, position);
    }

    public void addElement(IdentifierDescr identifierDescr) {
        getElements2().add(identifierDescr);
    }

    public List<IdentifierDescr> getElements() {
        List<IdentifierDescr> identifiers = new ArrayList<IdentifierDescr>();
        for (ElementDescriptor identifier : getElements2().getElementsByType(ElementType.IDENTIFIER)) {
            identifiers.add((IdentifierDescr)identifier);
        }
        return identifiers;
    }

    public String getName() {
        StringBuilder nameBuilder = new StringBuilder();
        boolean first = true;
        for (IdentifierDescr identifier : getElements()) {
            if (!first) {
                nameBuilder.append(".");
            }
            nameBuilder.append(identifier.getText());
            first = false;
        }
        return nameBuilder.toString();
    }
}