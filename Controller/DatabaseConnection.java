package Controller;

import java.sql.*;
import Model.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;

public class DatabaseConnection {
    public static Connection conn;

    public DatabaseConnection() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/4013_distributed_system", "root",
                    "Cheankoh1011."); // For MySQL only

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public ArrayList<Facility> getFacilityList() {
        try {
            Statement getFacility = conn.createStatement();
            ResultSet result = getFacility.executeQuery("Select * from Facility");
            ArrayList<Facility> facilities = new ArrayList<Facility>();
            while (result.next()) {
                Facility facility1 = new Facility();
                facility1.setFacilityName(result.getString("name"));
                facility1.setFacilityID(result.getInt("id"));
                facility1.setFacilityType(result.getInt("type"));
                // Generate Hashmap of availability out of a long string
                HashMap<Integer, Integer[][]> avail = new HashMap<>();
                String availability = result.getString("availability");
                // System.out.println("whole availability: " + availability);
                String[] eachDay = availability.split("_");
                for (String string : eachDay) {
                    // System.out.println("wholestring: " + string);
                    String[] day_avail = string.split("-");
                    // System.out.println("Length of day_avail : " + day_avail.length);
                    Integer day = Integer.parseInt(day_avail[0]);
                    String[] eachTimeSlot = day_avail[1].split(",");
                    // System.out.println("day : " + day);
                    // System.out.println("before split : " + day_avail[1]);
                    // System.out.println("all time slots : " + eachTimeSlot.toString());

                    Integer timeslot[][] = {};
                    for (String slot : eachTimeSlot) {
                        String[] isBooked_time = slot.split(":");
                        Integer[] timeSlot = { Integer.parseInt(isBooked_time[0]), Integer.parseInt(isBooked_time[1]) };
                        addElement(timeslot, timeSlot);
                        System.out.println(timeslot.toString() + "\n");
                    }
                    avail.put(day, timeslot);
                }

                facility1.setAvailability(avail);
                facilities.add(facility1);
            }
            return facilities;
        } catch (SQLException s) {
            // TODO: handle exception
            s.printStackTrace();
            return null;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return null;
        }

    }

    public ArrayList<Booking> getBookingList() {
        try {
            Statement getBooking = conn.createStatement();
            ResultSet result = getBooking.executeQuery("Select * from Booking");
            ArrayList<Booking> bookings = new ArrayList<Booking>();
            while (result.next()) {
                Booking booking = new Booking();
                booking.setUserID(result.getInt("userID"));
                booking.setFacilityID(result.getInt("facilityID"));
                booking.setBookingID(result.getInt("bookingID"));
                booking.setDate(result.getString("date"));
                String timing = result.getString("timing");
                String[] start_end = timing.split(",");
                ArrayList<Integer> intTiming = new ArrayList<Integer>();
                intTiming.add(Integer.parseInt(start_end[0]));
                intTiming.add(Integer.parseInt(start_end[1]));
                booking.setTiming(intTiming);
                bookings.add(booking);
            }
            return bookings;
        } catch (SQLException s) {
            // TODO: handle exception
            s.printStackTrace();
            return null;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return null;
        }
    }

    public Boolean updateFacility(ArrayList<Facility> facilities) {
        try {
            for (Facility facility : facilities) {
                PreparedStatement update = conn.prepareStatement("update Facility set availability=? where id=?");
                // Manually concatenate Hashmap as availability string
                // for simplicity
                HashMap<Integer, Integer[][]> avail = facility.getAvailability();
                String availString = "";
                for (int i = 1; i < 8; i++) { // Mon to Sun
                    availString += String.valueOf(i) + "-";
                    Integer[][] timeslot = avail.get(i);
                    for (Integer[] eachSlot : timeslot) {
                        availString += String.valueOf(eachSlot[0]) + ":" + String.valueOf(eachSlot[1]) + ",";
                    }
                    availString = availString.substring(0, availString.length() - 1);
                    availString += "_";
                }
                availString = availString.substring(0, availString.length() - 1);
                update.setString(1, availString);
                update.setInt(2, facility.getFacilityID());
                update.executeUpdate();
            }
            return true;
        } catch (SQLException s) {
            // TODO: handle exception
            s.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean updateBooking(ArrayList<Booking> bookings) {
        try {
            for (Booking booking : bookings) {
                PreparedStatement update = conn
                        .prepareStatement("update Booking set timing=? , date=? where bookingID=?");
                // Manually concatenate ArrayList<Integer> as timing string
                // for simplicity
                ArrayList<Integer> timing = booking.getTiming();
                String timingString = String.valueOf(timing.get(0)) + "," + String.valueOf(timing.get(1));

                update.setString(1, timingString);
                update.setString(2, booking.getDate());
                update.setInt(3, booking.getBookingID());
                update.executeUpdate();
                update.executeUpdate()
            }
            return true;
        } catch (SQLException s) {
            // TODO: handle exception
            s.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Integer createBooking(Booking booking) {
        try {
            PreparedStatement update = conn
                    .prepareStatement("Insert into Booking (userID, facilityID, date, timing) values (?, ?, ?, ?)");
            update.setInt(1, booking.getUserID());
            update.setInt(2, booking.getFacilityID());
            update.setString(3, booking.getDate());
            // Manually concatenate ArrayList<Integer> as timing string
            // for simplicity
            ArrayList<Integer> timing = booking.getTiming();
            String timingString = String.valueOf(timing.get(0)) + "," + String.valueOf(timing.get(1));
            update.setString(4, timingString);
            update.executeUpdate();
            ResultSet rs = update.getGeneratedKeys();
            Long longID = rs.getLong(1);
            Integer generatedBookingID = longID.intValue();
            booking.setBookingID(generatedBookingID);
            return generatedBookingID;
        } catch (SQLException s) {
            // TODO: handle exception
            s.printStackTrace();
            return -1;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return -1;
        }
    }

    public Boolean deleteBooking(int bookingID) {
        Boolean isValidID = true;
        try {
            PreparedStatement update = conn.prepareStatement("Delete from Booking where bookingID=?");
            update.setInt(1, bookingID);
            isValidID = (update.executeUpdate() > 0) ? true : false;
        } catch (SQLException s) {
            // TODO: handle exception
            s.printStackTrace();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return isValidID;
    }

    public static Integer[][] addElement(Integer[][] a, Integer[] e) {
        a = Arrays.copyOf(a, a.length + 1);
        a[a.length - 1] = e;
        return a;
    }
}
