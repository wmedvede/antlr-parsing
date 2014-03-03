package parser.metadata;


public class ElementDescriptor {

    public static enum ElementType {
        MODIFIER,
        FIELD,
        VARIABLE,
        VARIABLE_INITIALIZER,
        METHOD,
        CLASS,
        PRIMITIVE_TYPE,
        CLASS_OR_INTERFACE_TYPE,
        IDENTIFIER_WITH_TYPE_ARGUMENTS,
        TYPE_ARGUMENT,
        TYPE,
        NORMAL_PARAMETER,
        ELLIPSIS_PARAMETER,
        TEXT_TOKEN
    }

    private ElementType elementType;

    private int start;

    private int stop;

    private int line;

    private int position;

    private String text;

    public ElementDescriptor(ElementType elementType) {
        this.elementType = elementType;
    }

    public ElementDescriptor(ElementType elementType, String text, int start, int line, int position) {
        this.elementType = elementType;
        this.text = text;
        this.start = start;
        this.line = line;
        this.position = position;
    }

    public ElementDescriptor(ElementType elementType, String text, int start, int stop) {
        this.elementType = elementType;
        this.text = text;
        this.start = start;
        this.stop = stop;
    }

    public ElementDescriptor(ElementType elementType, String text, int start, int stop, int line, int position) {
        this.elementType = elementType;
        this.text = text;
        this.start = start;
        this.stop = stop;
        this.line = line;
        this.position = position;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStop() {
        return stop;
    }

    public void setStop(int stop) {
        this.stop = stop;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isElementType(ElementType elementType) {
        return this.elementType == elementType;
    }

}
