package co.wethinkcode.trafficflow;

public class Intersection {
    private String id;
    private String district;
    private String signalType;
    private Boolean active;

    public Intersection() {}

    public Intersection(String id, String district, String signalType, Boolean active) {
        this.id = id;
        this.district = district;
        this.signalType = signalType;
        this.active = active;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getSignalType() { return signalType; }
    public void setSignalType(String signalType) { this.signalType = signalType; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}