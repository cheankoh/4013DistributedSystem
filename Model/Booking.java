package Model;
import java.util.ArrayList;
import java.io.Serializable;
import java.time.*;

public class Booking implements Serializable{

    private int userID;
    private int facilityID;
    private int bookingID;
    private int date;
    private ArrayList<Integer> timing;




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

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public ArrayList<Integer> getTiming() {
        return timing;
    }

    public void setTiming(ArrayList<Integer> timing) {
        this.timing = timing;
    }

}
