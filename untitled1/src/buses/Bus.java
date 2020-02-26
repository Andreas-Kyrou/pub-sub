package buses;

import java.io.Serializable;

public class Bus implements Serializable {
    private String lineNumber;//linecode
    private String routeCode;//routecode
    private String lineName;//routetype
    private String buslineid;//lineid
    private String info;//description

    public String getLineName() {
        return lineName;
    }

    public String getInfo() {
        return info;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }

    public String getLineNumber() {
        return lineNumber;
    }



    public void setInfo(String info) {
        this.info = info;
    }

    public String getBuslineid() {
        return buslineid;
    }

    public void setBuslineid(String buslineid) {
        this.buslineid= buslineid;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }


}
