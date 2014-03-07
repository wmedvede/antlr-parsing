package parser;

import parser.metadata.ElementDescriptor;
import parser.metadata.FieldDescr;
import parser.metadata.MethodDescr;
import parser.metadata.VariableDeclarationDescr;
import util.ParserUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaCodeManager {

    private StringBuilder source = null;

    private JavaParser parser;

    private Map<ElementDescriptor, ElementDescriptor> deletedElements = new HashMap<ElementDescriptor, ElementDescriptor>();

    private Map<ElementDescriptor, ElementDescriptor> originalElements = new HashMap<ElementDescriptor, ElementDescriptor>();

    private List<ElementDescriptor> allElements = new ArrayList<ElementDescriptor>();

    public JavaCodeManager(InputStream inputStream) throws Exception {
        //TODO implement better exceptions handling
        source = new StringBuilder(ParserUtil.readString(inputStream));
        parseSource();
    }

    public void deleteField(String name) throws Exception {
        for (FieldDescr field : parser.fields) {
            int deletedDeclarations = 0;
            for (VariableDeclarationDescr var : field.getVariableDeclarations()) {
                if (var.getIdentifier().equals(name)) {
                    deleteElement(var);
                }
                if (deletedElements.containsKey(var)) {
                    deletedDeclarations++;
                }
            }
            if (deletedDeclarations == field.getVariableDeclarations().size()) deleteElement(field);
        }
    }

    public void deleteMethod(String name) throws Exception {
        //TODO implement overloading, constructors deletion, etc.
        for (MethodDescr method : parser.methods) {
            if (name.equals(method.getName())) {
                deleteElement(method);
            }
        }
    }

    public void addField(String sentences) {
        ElementDescriptor lastField = null;
        for (ElementDescriptor member : allElements) {
            if (member.getElementType() == ElementDescriptor.ElementType.FIELD) lastField = member;
        }
        addMemberAfter(lastField, ElementDescriptor.ElementType.FIELD, sentences);
    }

    public void addMethod(String sentences) {
        ElementDescriptor lastMethod = null;
        for (ElementDescriptor member : allElements) {
            if (member.getElementType() == ElementDescriptor.ElementType.METHOD) lastMethod = member;
        }
        addMemberAfter(lastMethod, ElementDescriptor.ElementType.METHOD, sentences);
    }

    public void addMember(String sentences) {
        allElements.add(allElements.size(), new ElementDescriptor(ElementDescriptor.ElementType.SENTENCE, sentences, -1, -1));
    }

    public String getOriginalContent() {
        return source.toString();
    }

    public String build() {

        StringBuilder dest = new StringBuilder(source.length());
        int sourceIndex = 0;

        for (ElementDescriptor member : allElements) {

            if (isOriginal(member)) {
                if (sourceIndex < member.getStart()) {
                    dest.append(source.substring(sourceIndex, member.getStart()));
                }
                if (!isDeleted(member)) {
                    copyOriginalElement(member, dest);
                }
                sourceIndex = member.getStop() + 1;
            } else if (!isDeleted(member)) {
                dest.append(member.getText());
            }
        }
        if (sourceIndex < source.length()) {
            dest.append(source.substring(sourceIndex, source.length()));
        }

        return dest.toString();
    }

    private void parseSource() throws Exception {
        parser = ParserUtil.initParser(source.toString());
        parser.compilationUnit();
        for (ElementDescriptor member : parser.getClassDesc().getMembers()) {
            originalElements.put(member, member);
            allElements.add(member);
        }
    }

    private void deleteElement(ElementDescriptor element) throws Exception {
        deletedElements.put(element, element);
    }

    private void copyOriginalElement(ElementDescriptor element, StringBuilder dest) {
        switch (element.getElementType()) {
            case FIELD:
                copyField((FieldDescr)element, dest);
                break;
            default:
                dest.append(source.substring(element.getStart(), element.getStop()+1));
        }
    }

    private void copyField(FieldDescr field, StringBuilder dest) {
        //TODO this method should be improved when we add annotations processing, etc.
        StringBuilder tmpBuilder = new StringBuilder( field.getStop() - field.getStart() );
        tmpBuilder.append(source.substring(field.getModifiers().getStart(), field.getModifiers().getStop()+1));
        int sourceIndex = field.getModifiers().getStop() + 1;
        boolean previousDeleted = false;

        for (VariableDeclarationDescr varDec : field.getVariableDeclarations()) {
            if (sourceIndex < varDec.getStart()) {
                tmpBuilder.append(source.substring(sourceIndex, varDec.getStart()));
            }
            if (previousDeleted) {
                //TODO this deletion should be improved.
                int index = tmpBuilder.lastIndexOf(",");
                if (index > 0) {
                    tmpBuilder.deleteCharAt(index);
                }
            }
            if (!isDeleted(varDec)) {
                tmpBuilder.append(source.substring(varDec.getStart(), varDec.getStop()+1));
                previousDeleted = false;
            } else {
                //check if a "," for needs to be deleted
                previousDeleted = true;
            }
            sourceIndex = varDec.getStop() +1;
        }
        if (previousDeleted) {
            //TODO this deletion should be improved.
            int index = tmpBuilder.lastIndexOf(",");
            if (index > 0) {
                tmpBuilder.deleteCharAt(index);
            }
        }
        if (sourceIndex <= field.getStop()) {
            tmpBuilder.append(source.substring(sourceIndex, field.getStop() + 1));
        }
        dest.append(tmpBuilder.toString());
    }

    private boolean isDeleted(ElementDescriptor element) {
        return deletedElements.containsKey(element);
    }

    private boolean isOriginal(ElementDescriptor element) {
        return originalElements.containsKey(element);
    }

    private void addMemberAfter(ElementDescriptor member, ElementDescriptor.ElementType type, String sentences) {
        int index = member != null ?  allElements.indexOf(member) : -1;
        index = index < 0 ? allElements.size() : (index+1);
        allElements.add(index, new ElementDescriptor(type, sentences, -1, -1));
    }

    private void addMemberBefore(ElementDescriptor member, ElementDescriptor.ElementType type, String sentences) {
        int index = member != null ?  allElements.indexOf(member) : -1;
        index = index < 0 ? 0 : (index+1);
        allElements.add(index, new ElementDescriptor(type, sentences, -1, -1));
    }

}