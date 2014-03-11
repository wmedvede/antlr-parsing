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

    public void addElement(TextTokenElementDescr element) {
        elements.add(element);
    }

    public List<TextTokenElementDescr> getElements() {
        return elements;
    }

    public String getName() {
        StringBuilder nameBuilder = new StringBuilder();
        boolean first = true;
        for (TextTokenElementDescr element : getElements()) {
            if (!first) {
                nameBuilder.append(".");
            }
            nameBuilder.append(element.getText());
            first = false;
        }
        return nameBuilder.toString();
    }
}