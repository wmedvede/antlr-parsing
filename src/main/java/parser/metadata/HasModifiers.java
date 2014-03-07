package parser.metadata;

public interface HasModifiers {

    ModifierListDescr getModifiers();

    void setModifiers(ModifierListDescr modifiers);

    void addModifier(ModifierDescr modifier);
}
