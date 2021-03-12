package Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import Model.Facility;

public class FacilityController {

    public static List<Integer[][]> queryAvailability(List<Integer> dayOfWeek, int facilityTypeID,
            int facilitySelection, ArrayList<Facility> facilityData) {

        List<Integer[][]> ResultAvailability = new ArrayList<Integer[][]>();

        // Filter and create a new arraylist to filter and store relevent facilities
        ArrayList<Facility> filteredFacilityList = new ArrayList<Facility>();
        for (Facility i : facilityData) {
            if ((i.getFacilityType() == facilityTypeID)) {
                filteredFacilityList.add(i);
            }
        }

        Facility targetFacility = filteredFacilityList.get(facilitySelection - 1);
        // facilitySelection = 1 :First facility in the respective facility type
        HashMap<Integer, Integer[][]> availability = targetFacility.getAvailability();

        System.out.println(dayOfWeek.get(0));
        System.out.println(dayOfWeek.get(1));
        if (dayOfWeek.get(1) == -1) {// Single day
            Integer[][] timeslot = availability.get(dayOfWeek.get(0));
            ResultAvailability.add(timeslot);
        } else {// Range of days
            for (int i = dayOfWeek.get(0); i <= dayOfWeek.get(1); i++) {
                Integer[][] timeslot = availability.get(i);
                ResultAvailability.add(timeslot);

            }

        }

        return ResultAvailability;
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
