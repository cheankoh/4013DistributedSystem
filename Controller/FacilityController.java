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
}
