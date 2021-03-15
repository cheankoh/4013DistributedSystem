package Controller;

import Model.Booking;
import Model.Facility;
import Model.User;
import Controller.FileIO;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;

class DataFactory{


    public static void main(String[] args) {
    Integer timeslot[][] = {{0,800},{0,830},{0,900},{0,930},{0,1000},{0,1030},{0,1100},{0,1130},{0,1200},{0,1230},{0,1300},{0,1330},{0,1400},{0,1430},{0,1500},{0,1530},{0,1600},{0,1630},{0,1700}};

    HashMap<Integer, Integer[][]> avail = new HashMap<Integer, Integer[][]>();
    avail.put(1, timeslot);
    avail.put(2, timeslot);
    avail.put(3, timeslot);
    avail.put(4, timeslot);
    avail.put(5, timeslot);
    avail.put(6, timeslot);
    avail.put(7, timeslot);
    
    
    Facility facility1 = new Facility();
    facility1.setFacilityName("Learning Pod 1");
    facility1.setFacilityID(1);
    facility1.setFacilityType(1);
    facility1.setAvailability(avail);
    Facility facility2 = new Facility();
    facility2.setFacilityName("Learning Pod 2");
    facility2.setFacilityID(2);
    facility2.setFacilityType(1);
    facility2.setAvailability(avail);
    Facility facility3 = new Facility();
    facility3.setFacilityName("Lecture Theatre 1");
    facility3.setFacilityID(3);
    facility3.setFacilityType(2);
    facility3.setAvailability(avail);
    Facility facility4 = new Facility();
    facility4.setFacilityName("Lecture Theatre 2");
    facility4.setFacilityID(4);
    facility4.setFacilityType(2);
    facility4.setAvailability(avail);
    Facility facility5 = new Facility();
    facility5.setFacilityName("Tutorial Room 1");
    facility5.setFacilityID(5);
    facility5.setFacilityType(3);
    facility5.setAvailability(avail);
    Facility facility6 = new Facility();
    facility6.setFacilityName("Tutorial Room 2");
    facility6.setFacilityID(6);
    facility6.setFacilityType(3);
    facility6.setAvailability(avail);
    Facility facility7 = new Facility();
    facility7.setFacilityName("Language Room 1");
    facility7.setFacilityID(7);
    facility7.setFacilityType(4);
    facility7.setAvailability(avail);
    Facility facility8 = new Facility();
    facility8.setFacilityName("Language Room 2");
    facility8.setFacilityID(8);
    facility8.setFacilityType(4);
    facility8.setAvailability(avail);


    ArrayList<Facility> Facilitylist= new ArrayList<Facility>();
    Facilitylist.add(facility1);
    Facilitylist.add(facility2);
    Facilitylist.add(facility3);
    Facilitylist.add(facility4);
    Facilitylist.add(facility5);
    Facilitylist.add(facility6);
    Facilitylist.add(facility7);
    Facilitylist.add(facility8);

    try{
        FileIO.writeObject("facilities.txt", Facilitylist);
    }catch (IOException e){
        System.out.println("File not found. please try again.");
    }
    
        // ArrayList<Booking> booking = new ArrayList<Booking>();
        // FileIO.storeBookingData(booking);

    
    // ArrayList<Facility> Facilitylistreceive= new ArrayList<Facility>();

    // try{
    //     Facilitylistreceive = (ArrayList<Facility>) FileIO.readObject("facilities.txt");
    // }
    // catch (ClassNotFoundException | IOException e) {
    //     System.out.println("File is missing. Please try again");
    // }
    // for (Facility i: Facilitylistreceive){
    //     System.out.println(i.getFacilityName());
    // }

    // User user1 = new User();
    // User user2 = new User();

    // user1.setUserID(1234);
    // user2.setUserID(4567);

    // ArrayList<User> Userlist= new ArrayList<User>();

    // Userlist.add(user1);
    // Userlist.add(user2);

    ////Write userlist object into testuser.txt
    //FileIO.storeUserData(user1);
    //User testUser = FileIO.getUserData();
    // try{
    //     FileIO.writeObject("testusers.txt", Userlist);
    // }catch (IOException e){
    //     System.out.println("File not found. please try again.");
    // }


    //Read testuser.txt and display user stored
    // ArrayList<User> Userlistreceive= new ArrayList<User>();

    // try{
    //     Userlistreceive = (ArrayList<User>) FileIO.readObject("testusers.txt");
    // }
    // catch (ClassNotFoundException | IOException e) {
    //     System.out.println("File is missing. Please try again");
    // }

    // System.out.println(Userlistreceive);
   



    }

}
