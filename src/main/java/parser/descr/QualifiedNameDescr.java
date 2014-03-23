package parser.descr;


import java.util.ArrayList;
import java.util.List;

/**
 * TODO next version should be improved dot chars are not being stored '.'.
 *
 */
public class QualifiedNameDescr extends ElementDescriptor {

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

    public void addPart(IdentifierDescr identifierDescr) {
        getElements().add(identifierDescr);
    }

    public List<IdentifierDescr> getParts() {
        List<IdentifierDescr> identifiers = new ArrayList<IdentifierDescr>();
        for (ElementDescriptor identifier : getElements().getElementsByType(ElementType.IDENTIFIER)) {
            identifiers.add((IdentifierDescr)identifier);
        }
        return identifiers;
    }

    public String getName() {
        StringBuilder nameBuilder = new StringBuilder();
        boolean first = true;
        for (IdentifierDescr identifier : getParts()) {
            if (!first) {
                nameBuilder.append(".");
            }
            nameBuilder.append(identifier.getIdentifier());
            first = false;
        }
        return nameBuilder.toString();
    }
}