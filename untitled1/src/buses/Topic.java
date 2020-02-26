package buses;

import java.io.Serializable;

public class Topic implements Serializable {
    private static final long serialVersionUID=2345762026229505876L;
    String busLine;

    public Topic(String busLine){
        super();
        this.busLine=busLine;
    }

    public void setBusLine(String busLine) {
        this.busLine = busLine;
    }

    public String getBusLine() {
        return busLine;
    }
}
