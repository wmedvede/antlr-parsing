package parser.metadata;

public class ModifiersContainerDescr extends ElementDescriptor implements HasModifiers {

    private ModifierListDescr modifiers = new ModifierListDescr();

    public ModifiersContainerDescr(ElementType elementType) {
        super(elementType);
    }

    public ModifiersContainerDescr(ElementType elementType, String text, int start, int line, int position) {
        super(elementType, text, start, line, position);
    }

    public ModifiersContainerDescr(ElementType elementType, String text, int start, int stop) {
        super(elementType, text, start, stop);
    }

    public ModifiersContainerDescr(ElementType elementType, String text, int start, int stop, int line, int position) {
        super(elementType, text, start, stop, line, position);
    }

    @Override
    public ModifierListDescr getModifiers() {
        return modifiers;
    }

    @Override
    public void setModifiers(ModifierListDescr modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public void addModifier(ModifierDescr modifier) {
        modifiers.add(modifier);
    }
}