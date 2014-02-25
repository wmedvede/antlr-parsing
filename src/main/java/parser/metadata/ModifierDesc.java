package parser.metadata;


public class ModifierDesc extends ElementDescriptor {

    private String name;

    public ModifierDesc() {
        super(ElementType.MODIFIER);
    }

    public ModifierDesc(String text, int start, int stop, String name) {
        this(text, start, stop, -1, -1, name);
    }

    public ModifierDesc(String text, int start, int stop, int line, int position, String name) {
        super(ElementType.MODIFIER, text, start, stop, line, position);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
