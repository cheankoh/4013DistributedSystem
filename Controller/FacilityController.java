package Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import Model.Facility;
import Model.Booking;

public class FacilityController {

    static int BookingID = 0;

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

        System.out.println("dayOfWeek.get(0): " + dayOfWeek.get(0));
        System.out.println("dayOfWeek.get(1): " + dayOfWeek.get(1));
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

    public static int[] bookFacility(ArrayList<Facility> facilityData, int facilityTypeID, int facilitySelection,
            int dayOfWeek, int startTime, int endTime, int userID, DatabaseConnection conn) {

        // Get list of facility in the selected facility type.
        ArrayList<Facility> filteredFacilityList = new ArrayList<Facility>();
        for (Facility i : facilityData) {
            if ((i.getFacilityType() == facilityTypeID)) {
                filteredFacilityList.add(i);
            }
        }
        if (facilitySelection > filteredFacilityList.size()) {

            int[] result = new int[2];
            result[0] = 3;
            result[1] = 0;
            return result; // Invalid selection for facility.
        }

        // get selected facility in the selected facility type
        Facility targetFacility = filteredFacilityList.get(facilitySelection - 1);

        // get availability of the selected facility
        HashMap<Integer, Integer[][]> availability = targetFacility.getAvailability();
        Integer[][] timeslot = availability.get(dayOfWeek); // Availability of the selected day. eg: Monday
        for (int j = startTime - 1; j < endTime; j++) {
            if (timeslot[j][0] != 0) {
                System.out.println("Booking failed: Timeslot already booked");

                int[] result = new int[2];
                result[0] = 2;
                result[1] = 0;
                return result;
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
        // FileIO.storeFacilityData(facilityData); // update facility.txt
        conn.updateFacility(facilityData);

        // Create a Booking object and save to database
        Booking newBooking = new Booking();
        newBooking.setUserID(userID);
        newBooking.setFacilityID(targetFacility.getFacilityID());
        newBooking.setBookingID(BookingID);
        BookingID++;
        newBooking.setDate(dayOfWeek);
        ArrayList<Integer> timing = new ArrayList<Integer>();
        timing.add(startTime);
        timing.add(endTime);
        newBooking.setTiming(timing);
        int bookingID = conn.createBooking(newBooking);
        // ArrayList<Booking> booking = new ArrayList<Booking>();
        // booking = FileIO.getBookingData();
        // booking.add(newBooking);
        // FileIO.storeBookingData(booking);//////// Delete this and call
        // connection.create(booking)////////
        int[] result = new int[2];
        result[0] = (bookingID > 0) ? 1 : 0; // 0 if failure (not likely to happen)
        result[1] = bookingID;
        return result; // Successfully booked and updated in database

    }

    public static int[] shiftBookingSlot(int bookingID, int offset, ArrayList<Facility> facilityData,
            DatabaseConnection conn) {

        Booking result = conn.deleteBooking(bookingID); // result == deleted booking
        System.out.println("bookingID passed : " + bookingID);
        // If success, cont.
        // Else return -1

        int[] shiftResult = new int[2];
        if (result == null) {

            shiftResult[0] = -1; // Invalid bookingID
            shiftResult[1] = 0;
            return shiftResult;

        }

        // Get getFacilityID, getDate(dayofWeek), getTiming(startTime,endTime) of
        // booking
        int userID = result.getUserID();
        // int _bookingID = result.getBookingID();
        int facilityID = result.getFacilityID();
        int date = result.getDate();
        ArrayList<Integer> timing = result.getTiming();
        int startTime = timing.get(0);
        int endTime = timing.get(1);

        // Get facility for given facility ID
        Facility targetFacility = new Facility();

        for (Facility i : facilityData) {
            if ((i.getFacilityID() == facilityID)) {
                targetFacility = i;
            }
        }

        HashMap<Integer, Integer[][]> availability = targetFacility.getAvailability();
        Integer[][] timeslot = availability.get(date); // Availability of the selected day. eg: Monday

        // Do a checking if shifting is available//
        // If offset is positive, check availability of shifted slots
        if (offset > 0) {
            if (endTime + offset > 17) {
                shiftResult[0] = -3; // Invalid shift
                shiftResult[1] = 0;
                return shiftResult;
            }
            for (int j = endTime; j < endTime + offset; j++) {
                if (timeslot[j][0] != 0) {
                    System.out.println("Shift failed: Timeslot already booked");

                    shiftResult = new int[2];
                    shiftResult[0] = -2; // Timeslot booked
                    shiftResult[1] = 0;
                    return shiftResult;
                }
            }
        } else {// Offset is negative, check availability of shifted slots
            if (startTime - 1 + offset < 0) {
                shiftResult[0] = -3; // Invalid shift
                shiftResult[1] = 0;
                return shiftResult;
            }
            for (int j = startTime - 1 + offset; j < startTime - 1; j++) {
                if (timeslot[j][0] != 0) {
                    System.out.println("Shift failed: Timeslot already booked");

                    shiftResult = new int[2];
                    shiftResult[0] = -2;
                    shiftResult[1] = 0;
                    return shiftResult;
                }
            }
        }

        // Set original availability to 0 (not Booked)
        for (int j = startTime - 1; j < endTime; j++) {
            timeslot[j][0] = 0;
        }
        // Set SHIFTED availability to 1 (Booked)
        for (int j = startTime - 1 + offset; j < endTime + offset; j++) {
            timeslot[j][0] = 1;
        }

        // Update data in facilities.txt
        availability.put(date, timeslot);
        targetFacility.setAvailability(availability);
        for (Facility i : facilityData) {
            if ((i.getFacilityID() == targetFacility.getFacilityID())) {
                i = targetFacility;
            }
        }
        // FileIO.storeFacilityData(facilityData); // update facility.txt
        conn.updateFacility(facilityData);

        // Create a Booking object and save to database
        Booking newBooking = new Booking();
        newBooking.setUserID(userID);
        newBooking.setFacilityID(targetFacility.getFacilityID());
        newBooking.setBookingID(BookingID);
        BookingID++;
        newBooking.setDate(date);
        ArrayList<Integer> shifted_timing = new ArrayList<Integer>();
        shifted_timing.add(startTime);
        shifted_timing.add(endTime);
        newBooking.setTiming(shifted_timing);
        int newBookingID = conn.createBooking(newBooking);
        // ArrayList<Booking> booking = new ArrayList<Booking>();
        // booking = FileIO.getBookingData();
        // booking.add(newBooking);
        // FileIO.storeBookingData(booking);//////// Delete this and call
        // connection.create(booking)////////

        shiftResult[0] = (newBookingID > 0) ? 1 : 0; // 0 is failure to add
        shiftResult[1] = newBookingID;
        return shiftResult; // Successfully booked and updated in database
    }

    // public int cancelBooking(int bookingID,ArrayList<Facility>
    // facilityData,ArrayList<Booking> bookingData, Connection connection) {
    public static int cancelBooking(int bookingID, ArrayList<Facility> facilityData, DatabaseConnection conn) {

        Booking result = conn.deleteBooking(bookingID); // result ==deleted booking

        // If success, cont.
        // Else return -1
        if (result == null) {
            return -1;
        }

        // Get getFacilityID, getDate(dayofWeek), getTiming(startTime,endTime) of
        // booking
        // int userID = result.getUserID();
        int facilityID = result.getFacilityID();
        // int _bookingID = result.getBookingID();
        int date = result.getDate();
        ArrayList<Integer> timing = result.getTiming();
        int startTime = timing.get(0);
        int endTime = timing.get(1);

        // Get facility for given facility ID
        Facility targetFacility = new Facility();

        for (Facility i : facilityData) {
            if ((i.getFacilityID() == facilityID)) {
                targetFacility = i;
            }
        }

        HashMap<Integer, Integer[][]> availability = targetFacility.getAvailability();
        Integer[][] timeslot = availability.get(date); // Availability of the selected day. eg: Monday

        // Set availability to 0 (not booked)
        for (int j = startTime - 1; j < endTime; j++) {
            timeslot[j][0] = 0;
        }

        // update Facility database
        availability.put(date, timeslot);
        targetFacility.setAvailability(availability);
        for (Facility i : facilityData) {
            if ((i.getFacilityID() == targetFacility.getFacilityID())) {
                i = targetFacility;
            }
        }
        // FileIO.storeFacilityData(facilityData); // update facility.txt
        conn.updateFacility(facilityData);

        // Return success
        return 1;

        // boolean validID = false;
        // if (!validID)
        // return -1;
        // else {
        // return 0;
        // }
    }

    public static int[] extendBookingSlot(int bookingID, int noOfSlots, ArrayList<Facility> facilityData,
            DatabaseConnection conn) {

        Booking result = conn.deleteBooking(bookingID); // result == deleted booking

        // If success, cont.
        // Else return -1

        int[] shiftResult = new int[2];
        if (result == null) {

            shiftResult[0] = -1; // Invalid bookingID
            shiftResult[1] = 0;
            return shiftResult;

        }

        // Get getFacilityID, getDate(dayofWeek), getTiming(startTime,endTime) of
        // booking
        int userID = result.getUserID();
        // int _bookingID = result.getBookingID();
        int facilityID = result.getFacilityID();
        int date = result.getDate();
        ArrayList<Integer> timing = result.getTiming();
        int startTime = timing.get(0);
        int endTime = timing.get(1);

        // Get facility for given facility ID
        Facility targetFacility = new Facility();

        for (Facility i : facilityData) {
            if ((i.getFacilityID() == facilityID)) {
                targetFacility = i;
            }
        }

        HashMap<Integer, Integer[][]> availability = targetFacility.getAvailability();
        Integer[][] timeslot = availability.get(date); // Availability of the selected day. eg: Monday

        // Do a checking if shifting is available//
        // If noOfSlots is positive, check availability of shifted slots
        if (noOfSlots > 0) {
            if (endTime + noOfSlots > 17) {
                shiftResult[0] = -3; // Invalid shift latest slot reached
                shiftResult[1] = 0;
                return shiftResult;
            }
            for (int j = startTime - 1; j < endTime + noOfSlots; j++) {
                if (timeslot[j][0] != 0) {
                    System.out.println("Shift failed: Timeslot already booked");

                    shiftResult = new int[2];
                    shiftResult[0] = -2; // Timeslot booked
                    shiftResult[1] = 0;
                    return shiftResult;
                }
            }
        } else {// noOfSlots is negative, check availability of shifted slots
            if (endTime - startTime + noOfSlots < 0) {
                shiftResult[0] = -4; // Invalid shift minimum timeslot length
                shiftResult[1] = 0;
                return shiftResult;
            }

        }

        // After check
        // if noOfSlots is positive
        if (noOfSlots > 0) {
            // Set availability of added slot to 1 (Booked)
            for (int j = startTime - 1; j < endTime + noOfSlots; j++) {
                timeslot[j][0] = 1;
            }

        } else {// noOfSlots is negative,

            // Set original availability to 0 (not Booked)
            for (int j = startTime - 1; j < endTime; j++) {
                timeslot[j][0] = 0;
            }

            // Set availability of start to end+offset to 1 (Booked)
            for (int j = startTime - 1; j < endTime + noOfSlots; j++) {
                timeslot[j][0] = 1;
            }
        }

        // Update data in facilities.txt
        availability.put(date, timeslot);
        targetFacility.setAvailability(availability);
        for (Facility i : facilityData) {
            if ((i.getFacilityID() == targetFacility.getFacilityID())) {
                i = targetFacility;
            }
        }
        // FileIO.storeFacilityData(facilityData); // update facility.txt
        conn.updateFacility(facilityData);

        // Create a new Booking object and save to database
        Booking newBooking = new Booking();
        newBooking.setUserID(userID);
        newBooking.setFacilityID(targetFacility.getFacilityID());
        newBooking.setBookingID(BookingID);
        BookingID++;
        newBooking.setDate(date);
        ArrayList<Integer> shifted_timing = new ArrayList<Integer>();
        shifted_timing.add(startTime);
        shifted_timing.add(endTime + noOfSlots); // take note
        newBooking.setTiming(shifted_timing);
        int newBookingID = conn.createBooking(newBooking);
        // ArrayList<Booking> booking = new ArrayList<Booking>();
        // booking = FileIO.getBookingData();
        // booking.add(newBooking);
        // FileIO.storeBookingData(booking);//////// Delete this and call
        // connection.create(booking)////////

        shiftResult[0] = (newBookingID > 0) ? 1 : 0; // 0 is failure to add
        shiftResult[1] = newBookingID;
        return shiftResult; // Successfully booked and updated in database
    }
}
