package co.wethinkcode.trafficflow;

public class Intersection {
    private String id;
    private String district;
    private String signalType;
    private Boolean active;

    public Intersection(String id, String district, String signalType, Boolean active) {
        this.id = id;
        this.district = district;
        this.signalType = signalType;
        this.active = active;
    }

    public String getId() { return id; }
    public String getDistrict() { return district; }
    public String getSignalType() { return signalType; }
    public Boolean getActive() { return active; }
}