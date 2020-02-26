package buses;

import java.io.Serializable;

public class Value implements Serializable {
    private static final long serialVersionUID=-1511811801060752568L;
    Bus bus;
    String vehicleID;
    double latitude;
    double longitude;
    String timestampOfBusPosition;
    public Value(Bus bus,double latitude,double longitude,String vegicleid){
        super();
        this.bus=bus;
        this.vehicleID=vegicleid;
        this.latitude=latitude;
        this.longitude=longitude;
    }
    public Value(Bus buss,double latitude,double longitude,String timestampOfBusPosition,String vehicleid){
        super();
        this.vehicleID=vehicleid;
        this.bus=buss;
        this.latitude=latitude;
        this.longitude=longitude;
        this.timestampOfBusPosition=timestampOfBusPosition;

    }

    public String getVehicleID() {
        return vehicleID;
    }
    public void setVehicleID(String vehicleID) {
        this.vehicleID = vehicleID;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public String getTimestampOfBusPosition() {
        return timestampOfBusPosition;
    }

    public void setTimestampOfBusPosition(String timestampOfBusPosition) {
        this.timestampOfBusPosition = timestampOfBusPosition;
    }

    public Bus getBus() {
        return bus;
    }
}
