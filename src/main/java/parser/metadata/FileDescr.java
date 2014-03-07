package parser.metadata;

public class FileDescr extends ElementDescriptor {

    //TODO add PackageDescr, ImportDescr

    private ClassDescr classDescr;

    public FileDescr() {
        super(ElementType.FILE);
    }

    public ClassDescr getClassDescr() {
        return classDescr;
    }

    public void setClassDescr(ClassDescr classDescr) {
        this.classDescr = classDescr;
    }
}
