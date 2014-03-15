package parser.descr;

import java.util.ArrayList;
import java.util.List;

public class ModifierListDescr extends ElementDescriptor {

    private List<ModifierDescr> elements = new ArrayList<ModifierDescr>();

    public ModifierListDescr() {
        super(ElementType.MODIFIER_LIST);
    }

    public ModifierListDescr(String text, int start, int line, int position) {
        this(text, start, -1, line, position);
    }

    public ModifierListDescr(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public ModifierListDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.MODIFIER_LIST, text, start, stop, line, position);
    }

    public void add(ModifierDescr modifierDescr) {
        //elements.add(modifierDescr);
        getElements2().add(modifierDescr);
    }

    public List<ModifierDescr> getElements() {
        List<ModifierDescr> modifiers = new ArrayList<ModifierDescr>();
        for (ElementDescriptor modifier :  getElements2().getElementsByType(ElementType.MODIFIER)) {
            modifiers.add((ModifierDescr)modifier);
        }
        return modifiers;

        //return elements;
    }

    /*
    public void setElements(List<ModifierDescr> elements) {
        this.elements = elements;
    }
    */
}
