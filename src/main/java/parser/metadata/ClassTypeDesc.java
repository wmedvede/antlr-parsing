package parser.metadata;


public class ClassTypeDesc extends TypeDesc {

    public ClassTypeDesc() {
        super(ElementType.CLASS_TYPE);
    }

    public ClassTypeDesc(String text, int start, int stop, int line, int position, String name) {
        super(ElementType.CLASS_TYPE, text, start, stop, line, position, name);
    }

    public ClassTypeDesc(String text, int start, int stop, String name) {
        this(text, start, stop, -1, -1, name);
    }
}
