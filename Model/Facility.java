package Model;

import java.util.HashMap;

public class Facility {
    private String facilityName;
    private int facilityType;
    private int facilityID;
    private HashMap<Integer, Integer[]> availability;

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public int getFacilityType() {
        return facilityType;
    }

    public void setFacilityType(int facilityType) {
        this.facilityType = facilityType;
    }

    public int getFacilityID() {
        return facilityID;
    }

    public void setFacilityID(int facilityID) {
        this.facilityID = facilityID;
    }

    public HashMap<Integer, Integer[]> getAvailability() {
        return availability;
    }

    public void setAvailability(HashMap<Integer, Integer[]> availability) {
        this.availability = availability;
    }
    

 
}
