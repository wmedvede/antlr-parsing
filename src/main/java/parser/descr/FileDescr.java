package parser.descr;

public class FileDescr extends ElementDescriptor {

    //TODO add PackageDescr, ImportDescr

    public FileDescr() {
        super(ElementType.FILE);
    }

    public ClassDescr getClassDescr() {
        return (ClassDescr)elements.getFirst(ElementType.CLASS);
    }

    public void setClassDescr(ClassDescr classDescr) {
        getElements2().removeFirst(ElementType.CLASS);
        getElements2().add(classDescr);
    }

    public PackageDescr getPackageDescr() {
        return (PackageDescr)getElements2().getFirst(ElementType.PACKAGE);
    }

    public void setPackageDescr(PackageDescr packageDescr) {
        getElements2().removeFirst(ElementType.PACKAGE);
        getElements2().add(packageDescr);
    }
}
