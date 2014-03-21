package parser.descr;

public class PackageDescr extends ElementDescriptor {

    public PackageDescr() {
        super(ElementType.PACKAGE);
    }

    public PackageDescr(String text, int start, int line, int position) {
        this(text, start, -1, line, position);
    }

    public PackageDescr(String text, int start, int stop) {
        this(text, start, stop, -1, -1);
    }

    public PackageDescr(String text, int start, int stop, int line, int position) {
        super(ElementType.PACKAGE, text, start, stop, line, position);
    }

    public QualifiedNameDescr getQualifiedName() {
        return (QualifiedNameDescr)getElements2().getFirst(ElementType.QUALIFIED_NAME);
    }

    public void setQualifiedName(QualifiedNameDescr qualifiedName) {
        getElements2().removeFirst(ElementType.QUALIFIED_NAME);
        getElements2().add(qualifiedName);
    }

    public JavaTokenDescr getPackageToken() {
        return (JavaTokenDescr)getElements2().getFirst(ElementType.JAVA_PACKAGE);
    }

    public void setPackageToken(JavaTokenDescr packageToken) {
        getElements2().removeFirst(ElementType.JAVA_PACKAGE);
        getElements2().add(packageToken);
    }

    public String getPackageName() {
        return getQualifiedName() != null ? getQualifiedName().getName() : null;
    }

    public JavaTokenDescr getEndSemiColon() {
        return (JavaTokenDescr)getElements2().getLast(ElementType.JAVA_SEMI_COLON);
    }

    public PackageDescr setEndSemiColon(JavaTokenDescr element) {
        getElements2().removeFirst(ElementType.JAVA_SEMI_COLON);
        getElements2().add(element);
        return this;
    }

}
