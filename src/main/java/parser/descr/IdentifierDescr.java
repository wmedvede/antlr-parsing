package parser.descr;

public class IdentifierDescr extends ElementDescriptor {

    public IdentifierDescr() {
        super(ElementType.IDENTIFIER);
    }

    public IdentifierDescr(String text, int start, int line, int position) {
        this(text, start, -1, line, position);
    }

    public IdentifierDescr(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public IdentifierDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.IDENTIFIER, text, start, stop, line, position);
    }

    public String getIdentifier() {
        return getText();
    }
}
