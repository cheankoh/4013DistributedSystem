package Model;

import java.util.HashMap;
import java.io.Serializable;

public class Facility implements Serializable{
    private String facilityName;
    private int facilityType;
    // 1 = Learning Pod
    // 2 = Lecture Theatre
    // 3 = Tutorial Room
    // 4 = Language Room
    

    private int facilityID;
    private HashMap<Integer, Integer[][]> availability;

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

    public HashMap<Integer, Integer[][]> getAvailability() {
        return availability;
    }

    public void setAvailability(HashMap<Integer, Integer[][]> availability) {
        this.availability = availability;
    }
    

 
}
