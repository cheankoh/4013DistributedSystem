package Controller;

import Model.Facility;

public class FacilityController {
    public Facility queryAvailability(int[] dayOfWeek, int facilityID) {
        return new Facility();
    }

    public int bookFacility(int facilityID, int startTime, int noOfSlots) {
        return 0;
    }

    public String shiftBookingSlot(int bookingID, int offset) {
        return "Successfully shifted.";
    }

    public int cancelBooking(int bookingID) {
        boolean validID = false;
        if (!validID)
            return -1;
        else {
            return 0;
        }
    }

    public int extendBookingSlot(int bookingID, int noOfSlots) {
        System.out.println("Hi");
        return 0;
    }
}
