package parser.descr;


public class TextTokenElementDescr extends ElementDescriptor {

    public TextTokenElementDescr() {
        super(ElementType.TEXT_TOKEN);
    }

    public TextTokenElementDescr(String text, int line, int position) {
        super(ElementType.TEXT_TOKEN, text, -1, -1, line, position);
    }

    public TextTokenElementDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.TEXT_TOKEN, text, start, stop, line, position);
    }
}
