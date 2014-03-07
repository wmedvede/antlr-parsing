package parser.descr;

public interface HasModifiers {

    ModifierListDescr getModifiers();

    void setModifiers(ModifierListDescr modifiers);

    void addModifier(ModifierDescr modifier);
}
