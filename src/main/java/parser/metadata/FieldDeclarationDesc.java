package parser.metadata;


import java.util.ArrayList;
import java.util.List;

public class FieldDeclarationDesc extends ElementDescriptor {

    private List<ModifierDesc> modifiers = new ArrayList<ModifierDesc>();

    private List<VariableDeclarationDesc> variableDeclarations = new ArrayList<VariableDeclarationDesc>();

    private TypeDesc type;

    public FieldDeclarationDesc() {
        super(ElementType.FIELD);
    }

    public FieldDeclarationDesc(String text, int start, int end) {
        this(text, start, end, -1, -1);
    }

    public FieldDeclarationDesc(String text, int start, int stop, int line, int position) {
        super(ElementType.FIELD, text, start, stop, line, position);
    }

    public List<ModifierDesc> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<ModifierDesc> modifiers) {
        this.modifiers = modifiers;
    }

    public void addModifier(ModifierDesc modifierDesc) {
        if (modifiers == null) modifiers = new ArrayList<ModifierDesc>();
        modifiers.add(modifierDesc);
    }

    public TypeDesc getType() {
        return type;
    }

    public void setType(TypeDesc type) {
        this.type = type;
    }

    public List<VariableDeclarationDesc> getVariableDeclarations() {
        return variableDeclarations;
    }

    public void setVariableDeclarations(List<VariableDeclarationDesc> variableDeclarations) {
        this.variableDeclarations = variableDeclarations;
    }

    public void addVariableDeclaration(VariableDeclarationDesc variableDeclarationDesc) {
        if (variableDeclarations == null) variableDeclarations = new ArrayList<VariableDeclarationDesc>();
        variableDeclarations.add(variableDeclarationDesc);
    }
}
