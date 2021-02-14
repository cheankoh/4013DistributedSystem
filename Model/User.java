package Model;

import java.util.ArrayList;

public class User {
    private String userName;
    private int userID;
    private ArrayList<Booking> userBooking;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public ArrayList<Booking> getUserBooking() {
        return userBooking;
    }

    public void setUserBooking(ArrayList<Booking> userBooking) {
        this.userBooking = userBooking;
    }

}

