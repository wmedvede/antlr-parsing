package parser.descr;

public class ModifiersContainerDescr extends ElementDescriptor implements HasModifiers {

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
        //TODO review
        return (ModifierListDescr)getElements2().getFirst(ElementType.MODIFIER_LIST);
    }

    @Override
    public void setModifiers(ModifierListDescr modifiers) {
        //TODO ensure modifiers list is inserted in order
        getElements2().add(modifiers);
    }

    @Override
    public void addModifier(ModifierDescr modifier) {
        ModifierListDescr modifierListDescr = getModifiers();
        if (modifierListDescr == null) {
            modifierListDescr = new ModifierListDescr();
            getElements2().add(modifierListDescr);
        }
        if (modifierListDescr != null) modifierListDescr.add(modifier);
    }
}