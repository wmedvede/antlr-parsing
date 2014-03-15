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
        elements.removeFirst(ElementType.CLASS);
        elements.add(classDescr);
    }

    public PackageDescr getPackageDescr() {
        return (PackageDescr)elements.getFirst(ElementType.PACKAGE);
    }

    public void setPackageDescr(PackageDescr packageDescr) {
        //elements.removeFirst(ElementType.PACKAGE);
        //elements.add(packageDescr);
    }
}
