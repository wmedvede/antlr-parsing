package parser.metadata;


public class ModifierDescr extends ElementDescriptor {

    private String name;

    public ModifierDescr() {
        super(ElementType.MODIFIER);
    }

    public ModifierDescr(String text, int start, int stop, String name) {
        this(text, start, stop, -1, -1, name);
    }

    public ModifierDescr(String text, int start, int stop, int line, int position, String name) {
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
