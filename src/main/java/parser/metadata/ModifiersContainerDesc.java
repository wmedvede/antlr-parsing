package parser.metadata;

import java.util.ArrayList;
import java.util.List;

public class ModifiersContainerDesc extends ElementDescriptor implements HasModifiers {

    private List<ModifierDesc> modifiers = new ArrayList<ModifierDesc>();

    public ModifiersContainerDesc(ElementType elementType) {
        super(elementType);
    }

    public ModifiersContainerDesc(ElementType elementType, String text, int start, int line, int position) {
        super(elementType, text, start, line, position);
    }

    public ModifiersContainerDesc(ElementType elementType, String text, int start, int stop) {
        super(elementType, text, start, stop);
    }

    public ModifiersContainerDesc(ElementType elementType, String text, int start, int stop, int line, int position) {
        super(elementType, text, start, stop, line, position);
    }

    @Override
    public List<ModifierDesc> getModifiers() {
        return modifiers;
    }

    @Override
    public void addModifier(ModifierDesc modifierDesc) {
        modifiers.add(modifierDesc);
    }
}
