package parser.metadata;


public class DimensionDescr {

    private TextTokenElementDescr start;

    private TextTokenElementDescr stop;

    public DimensionDescr() {
    }

    public DimensionDescr(String startText, int startLine, int startPosition, String stopText, int stopLine, int stopPosition) {
        start = new TextTokenElementDescr(startText, startLine, startPosition);
        stop = new TextTokenElementDescr(stopText, stopLine, stopPosition);
    }

    public TextTokenElementDescr getStart() {
        return start;
    }

    public void setStart(TextTokenElementDescr start) {
        this.start = start;
    }

    public TextTokenElementDescr getStop() {
        return stop;
    }

    public void setStop(TextTokenElementDescr stop) {
        this.stop = stop;
    }
}
