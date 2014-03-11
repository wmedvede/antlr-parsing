package parser.descr;

public class PackageDescr extends ElementDescriptor {

    private QualifiedNameDescr qualifiedName;

    private TextTokenElementDescr packageToken;

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
        return qualifiedName;
    }

    public void setQualifiedName(QualifiedNameDescr qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public TextTokenElementDescr getPackageToken() {
        return packageToken;
    }

    public void setPackageToken(TextTokenElementDescr packageToken) {
        this.packageToken = packageToken;
    }

    public String getPackageName() {
        return qualifiedName != null ? qualifiedName.getName() : null;
    }
}
