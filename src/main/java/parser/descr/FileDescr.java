package parser.descr;

import java.util.ArrayList;
import java.util.List;

public class FileDescr extends ElementDescriptor {

    public FileDescr() {
        super(ElementType.FILE);
    }

    public PackageDescr getPackageDescr() {
        return (PackageDescr) getElements().getFirst(ElementType.PACKAGE);
    }

    public void setPackageDescr(PackageDescr packageDescr) {
        getElements().removeFirst(ElementType.PACKAGE);
        getElements().add(packageDescr);
    }

    public void addImport(ImportDescr importDescr) {
        getElements().add(importDescr);
    }

    public List<ImportDescr> getImports() {
        List<ImportDescr> imports = new ArrayList<ImportDescr>();
        for (ElementDescriptor member :  getElements().getElementsByType(ElementType.IMPORT)) {
            imports.add((ImportDescr)member);
        }
        return imports;
    }

    public ClassDescr getClassDescr() {
        return (ClassDescr) getElements().getFirst(ElementType.CLASS);
    }

    public void setClassDescr(ClassDescr classDescr) {
        getElements().removeFirst(ElementType.CLASS);
        getElements().add(classDescr);
    }

}
