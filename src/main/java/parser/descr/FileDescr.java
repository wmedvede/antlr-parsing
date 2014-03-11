package parser.descr;

public class FileDescr extends ElementDescriptor {

    //TODO add PackageDescr, ImportDescr

    private PackageDescr packageDescr;

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

    public PackageDescr getPackageDescr() {
        return packageDescr;
    }

    public void setPackageDescr(PackageDescr packageDescr) {
        this.packageDescr = packageDescr;
    }
}
