package Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import Model.Facility;
import Model.Booking;

public class FacilityController {

    static int BookingID =0;
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

        System.out.println("dayOfWeek.get(0): "+dayOfWeek.get(0));
        System.out.println("dayOfWeek.get(1): "+dayOfWeek.get(1));
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

    public static int bookFacility(ArrayList<Facility> facilityData, int facilityTypeID, int facilitySelection,
            int dayOfWeek, int startTime, int endTime,int userID) {
        Boolean found = false;
        ArrayList<Facility> filteredFacilityList = new ArrayList<Facility>();
        for (Facility i : facilityData) {
            if ((i.getFacilityType() == facilityTypeID)) {
                filteredFacilityList.add(i);
            }
        }
        if (facilitySelection > filteredFacilityList.size()) {
            return 3; // Invalid selection for facility.
        }
        Facility targetFacility = filteredFacilityList.get(facilitySelection - 1);

        HashMap<Integer, Integer[][]> availability = targetFacility.getAvailability(); // Availability of the selected
                                                                                       // facility
        Integer[][] timeslot = availability.get(dayOfWeek); // Availability of the selected day. eg: Monday
        for (int j = startTime - 1; j < endTime; j++) {
            if (timeslot[j][0] != 0) {
                System.out.println("Booking failed: Timeslot already booked");
                return 2;
            }
        }

        // Set availability to 1 (Booked)
        for (int j = startTime - 1; j < endTime; j++) {
            timeslot[j][0] = 1;
        }
        // Update data in facilities.txt
        availability.put(dayOfWeek, timeslot);
        targetFacility.setAvailability(availability);
        for (Facility i : facilityData) {
            if ((i.getFacilityID() == targetFacility.getFacilityID())) {
                i = targetFacility;
            }
        }
        FileIO.storeFacilityData(facilityData); // update facility.txt

        // Create a Booking object and save to database
        Booking newBooking = new Booking();

 
        newBooking.setUserID(userID);
        newBooking.setFacilityID(targetFacility.getFacilityID());
        newBooking.setBookingID(BookingID);
        BookingID++;
        
        newBooking.setDate("day: "  + Integer.toString(dayOfWeek)+ "startSelection: "+ Integer.toString(startTime)+ "endSelection: "+ Integer.toString(endTime));
        ArrayList<Integer> timing = new ArrayList<Integer>();
        timing.add(startTime);
        timing.add(endTime);
        newBooking.setTiming(timing);
        ArrayList<Booking> booking = new ArrayList<Booking>();
        booking = FileIO.getBookingData();
        booking.add(newBooking);
        FileIO.storeBookingData(booking);
        return 1; // Successfully booked and updated in database

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
