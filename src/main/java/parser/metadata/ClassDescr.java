package parser.metadata;


import java.util.ArrayList;
import java.util.List;

public class ClassDescr extends ElementDescriptor {

    List<ElementDescriptor> members = new ArrayList<ElementDescriptor>();

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
        members.add(member);
    }

    public List<ElementDescriptor> getMembers() {
        return members;
    }
}