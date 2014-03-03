package parser.metadata;


public class TextTokenElementDescriptor extends ElementDescriptor {

    public TextTokenElementDescriptor() {
        super(ElementType.TEXT_TOKEN);
    }

    public TextTokenElementDescriptor(String text, int line, int position) {
        super(ElementType.TEXT_TOKEN, text, line, position);
    }
}
