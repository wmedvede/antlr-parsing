package parser;

import parser.descr.*;
import parser.util.ParserUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaFileHandlerImpl implements JavaFileHandler {

    private StringBuilder source = null;

    private JavaParser parser;

    private FileDescr fileDescr;

    private Map<ElementDescriptor, ElementDescriptor> deletedElements = new HashMap<ElementDescriptor, ElementDescriptor>();

    private Map<ElementDescriptor, ElementDescriptor> originalElements = new HashMap<ElementDescriptor, ElementDescriptor>();

    private List<ElementDescriptor> allElements = new ArrayList<ElementDescriptor>();

    public JavaFileHandlerImpl(InputStream inputStream) throws Exception {
        //TODO implement better exceptions handling
        source = new StringBuilder(ParserUtil.readString(inputStream));
        parseSource();
    }

    @Override
    public FileDescr getFileDescr() {
        return fileDescr;
    }

    @Override
    public void createImport(String source) {
        //TODO
    }

    @Override
    public void removePackageImport(String packageName) {
        //TODO
    }

    @Override
    public void removeClassImport(String className) {
        //TODO
    }

    @Override
    public void setPackageName(String name) {
        //TODO
    }

    @Override
    public void createClassAnnotation(String source) {
        //TODO
    }

    @Override
    public void deleteClassAnnotation(String annotationClassName) {
        //TODO
    }

    @Override
    public void setClassName(String name) {
        //TODO
    }

    @Override
    public void setSuperClassName(String className) {
        //TODO
    }

    @Override
    public void createField(String source) {
        ElementDescriptor lastField = null;
        for (ElementDescriptor member : allElements) {
            if (member.getElementType() == ElementDescriptor.ElementType.FIELD) lastField = member;
        }
        addMemberAfter(lastField, ElementDescriptor.ElementType.FIELD, source);
    }

    @Override
    public void renameField(String name, String newName) {
        //TODO
    }

    @Override
    public void deleteField(String name) {
        if (fileDescr.getClassDescr() != null) {
            for (FieldDescr field : fileDescr.getClassDescr().getFields()) {
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
    }

    @Override
    public void createFieldAnnotation(String fieldName, String source) {
        //TODO
    }

    @Override
    public void deleteFieldAnnotation(String fieldName, String annotationClassName) {
        //TODO
    }

    @Override
    public void createMethod(String sentences) {
        ElementDescriptor lastMethod = null;
        for (ElementDescriptor member : allElements) {
            if (member.getElementType() == ElementDescriptor.ElementType.METHOD) lastMethod = member;
        }
        addMemberAfter(lastMethod, ElementDescriptor.ElementType.METHOD, sentences);
    }

    @Override
    public void deleteMethod(String name, String[] paramTypes) {
        //TODO implement overloading, constructors deletion, etc.
        if (fileDescr.getClassDescr() != null) {
            for (MethodDescr method : fileDescr.getClassDescr().getMethods()) {
                if (name.equals(method.getName())) {
                    deleteElement(method);
                }
            }
        }
    }

    @Override
    public String getOriginalContent() {
        return source.toString();
    }

    @Override
    public String buildResult() {

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
        parser = JavaParserFactory.newParser(source.toString());
        parser.compilationUnit();
        fileDescr = parser.getFileDescr();
        if (fileDescr.getPackageDescr() != null) {
            originalElements.put(fileDescr.getPackageDescr(), fileDescr.getPackageDescr());
            allElements.add(fileDescr.getPackageDescr());
        }
        if (fileDescr.getClassDescr() != null) {
            for (ElementDescriptor member : fileDescr.getClassDescr().getMembers()) {
                originalElements.put(member, member);
                allElements.add(member);
            }
        }
    }

    private void deleteElement(ElementDescriptor element) {
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


    public void populateUnManagedElements(ElementDescriptor element) {
        populateUnManagedElements(element.getStart(), element);
    }

    public void populateUnManagedElements(int startIndex, ElementDescriptor element) {

        String text;
        TextTokenElementDescr unmanagedToken;

        if (element.getElements2().size() > 0) {
            List<ElementDescriptor> originalElements = new ArrayList<ElementDescriptor>();
            originalElements.addAll(element.getElements2());

            for (ElementDescriptor child : originalElements) {
                if (startIndex < child.getStart()) {
                    unmanagedToken = new TextTokenElementDescr();
                    unmanagedToken.setStart(startIndex);
                    unmanagedToken.setStop(child.getStart()-1);
                    unmanagedToken.setSourceBuffer(child.getSourceBuffer());

                    text = unmanagedToken.getSourceBuffer().substring(unmanagedToken.getStart(), unmanagedToken.getStop() +1);
                    unmanagedToken.setText(text);

                    element.getElements2().addMemberBefore(child, unmanagedToken);
                }
                startIndex = child.getStop() + 1;

                populateUnManagedElements(child);
            }

            if (startIndex < element.getStop()) {
                unmanagedToken = new TextTokenElementDescr();
                unmanagedToken.setStart(startIndex);
                unmanagedToken.setStop(element.getStop());
                unmanagedToken.setSourceBuffer(element.getSourceBuffer());

                text = unmanagedToken.getSourceBuffer().substring(unmanagedToken.getStart(), unmanagedToken.getStop() +1);
                unmanagedToken.setText(text);
                element.getElements2().add(unmanagedToken);
            }
        }
    }

    public String printTreeOLD(ElementDescriptor element) {
        StringBuilder result = new StringBuilder();
        if (element.getElements2().size() == 0) {
            result.append(source.substring(element.getStart(), element.getStop() +1));
        } else {
            for (ElementDescriptor child : element.getElements2()) {
                result.append(printTreeOLD(child));
            }
        }
        return result.toString();
    }

    public String printTree(ElementDescriptor element) {
        StringBuilder result = new StringBuilder();
        if (element.getElements2().size() == 0) {
            result.append(element.getSourceBuffer().substring(element.getStart(), element.getStop() +1));
        } else {
            for (ElementDescriptor child : element.getElements2()) {
                result.append(printTree(child));
            }
        }
        return result.toString();
    }

    //temporal to not touch the parser
    public void setSourceBufferTMP(ElementDescriptor element, StringBuilder source) {
        element.setSourceBuffer(source);
        for (ElementDescriptor child : element.getElements2()) {
            setSourceBufferTMP(child, source);
        }
    }

}