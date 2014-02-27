package parser.metadata;

import java.util.List;

public interface HasModifiers {

    List<ModifierDesc> getModifiers();

    void addModifier(ModifierDesc modifierDesc);
}
