package parser.descr;


import java.util.ArrayList;
import java.util.List;

public class ClassDescr extends ModifiersContainerDescr {

    private String name;

    private TypeDescr superClass;

    private TextTokenElementDescr classToken;

    private TextTokenElementDescr extendsToken;

    /*
    //TODO add:
    //Super class
    //List of implemented interfaces
    //Type annotations.
    */

    public ClassDescr() {
        super(ElementType.CLASS);
    }

    public ClassDescr(String text, int start, int line, int position) {
        this(text, start, -1, line, position);
    }

    public ClassDescr(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public ClassDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.CLASS, text, start, stop, line, position);
    }

    public void addMember(ElementDescriptor member) {
        getElements2().add(member);
    }

    public List<ElementDescriptor> getMembers() {
        return getElements2();
    }

    public void addField(FieldDescr fieldDescr) {
        int index = getElements2().lastIndexOf(ElementType.FIELD);
        getElements2().add(index +1, fieldDescr);
    }

    public List<MethodDescr> getMethods() {

        /*
        List<MethodDescr> methods = new ArrayList<MethodDescr>();
        for (ElementDescriptor member : members) {
            if (ElementType.METHOD == member.getElementType()) methods.add((MethodDescr)member);
        }
        return methods;
        */


        List<MethodDescr> methods = new ArrayList<MethodDescr>();
        for (ElementDescriptor member :  getElements2().getElementsByType(ElementType.METHOD)) {
            methods.add((MethodDescr)member);
        }
        return methods;

    }

    public List<FieldDescr> getFields() {
        /*
        List<FieldDescr> fields = new ArrayList<FieldDescr>();
        for (ElementDescriptor member : members) {
            if (ElementType.FIELD == member.getElementType()) fields.add((FieldDescr)member);
        }
        return fields;
        */
        List<FieldDescr> fields = new ArrayList<FieldDescr>();
        for (ElementDescriptor member : getElements2().getElementsByType(ElementType.FIELD)) {
            fields.add((FieldDescr)member);
        }
        return fields;
    }

    public TextTokenElementDescr getClassToken() {
        return classToken;
    }

    public void setClassToken(TextTokenElementDescr classToken) {
        this.classToken = classToken;
    }

    public TextTokenElementDescr getExtendsToken() {
        return extendsToken;
    }

    public void setExtendsToken(TextTokenElementDescr extendsToken) {
        this.extendsToken = extendsToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TypeDescr getSuperClass() {
        return superClass;
    }

    public void setSuperClass(TypeDescr superClass) {
        this.superClass = superClass;
    }
}