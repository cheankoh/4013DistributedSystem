package Model;
import java.util.ArrayList;
import java.time.*;

public class Booking {

    private int userID;
    private int facilityID;
    private int bookingID;
    private String date;
    private ArrayList<Double> timing;




    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getFacilityID() {
        return facilityID;
    }

    public void setFacilityID(int facilityID) {
        this.facilityID = facilityID;
    }

    public int getBookingID() {
        return bookingID;
    }

    public void setBookingID(int bookingID) {
        this.bookingID = bookingID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<Double> getTiming() {
        return timing;
    }

    public void setTiming(ArrayList<Double> timing) {
        this.timing = timing;
    }

}
