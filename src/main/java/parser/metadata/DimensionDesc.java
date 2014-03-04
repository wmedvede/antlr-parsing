package parser.metadata;


public class DimensionDesc {

    private TextTokenElementDescriptor start;

    private TextTokenElementDescriptor stop;

    public DimensionDesc() {
    }

    public DimensionDesc(String startText, int startLine, int startPosition, String stopText, int stopLine, int stopPosition) {
        start = new TextTokenElementDescriptor(startText, startLine, startPosition);
        stop = new TextTokenElementDescriptor(stopText, stopLine, stopPosition);
    }

    public TextTokenElementDescriptor getStart() {
        return start;
    }

    public void setStart(TextTokenElementDescriptor start) {
        this.start = start;
    }

    public TextTokenElementDescriptor getStop() {
        return stop;
    }

    public void setStop(TextTokenElementDescriptor stop) {
        this.stop = stop;
    }
}
