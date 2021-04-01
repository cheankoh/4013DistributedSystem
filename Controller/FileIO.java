package Controller;
import Model.Booking;
import Model.Facility;
import Model.User;


import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

//For testing purposes. Not for actual application.
public class FileIO {

   public static ObjectOutputStream getObjectOutputStream(String fileName) {
      try {
         return new ObjectOutputStream(new FileOutputStream(fileName));
      } catch (IOException e) {
         e.printStackTrace();
      }
      return null;
   }

   
    // Write object to file

   public static void writeObject(String fileName, Object data) throws IOException {
      getObjectOutputStream(fileName).writeObject(data);
   }

    
     //get Object input stream
 
   public static ObjectInputStream getObjectInputStream(String fileName) {
      try {
         return new ObjectInputStream(new FileInputStream(fileName));
      } catch (IOException e) {
         e.printStackTrace();
      }
      return null;
   }

    
     //Read object from file
  
   public static Object readObject(String fileName) throws IOException, ClassNotFoundException {
      return (Object)getObjectInputStream(fileName).readObject();
   }




   
    
     //Writes User object to users.txt
   
   public static void storeUserData(User c) {
      try {
         ObjectOutputStream oos = getObjectOutputStream("users.txt");
         oos.writeObject(c);
      } catch (IOException e) {			
         System.out.println(e.getMessage());
      }
   }

    /**
     * Get User object from users.txt
     * @return User object
     */
   public static User getUserData() {
      try {
         return (User)getObjectInputStream("users.txt").readObject();
      } catch (ClassNotFoundException e) {
         System.out.println(e.getMessage());
      } catch (IOException e) {
         System.out.println(e.getMessage());
      }
      return null;
   }

   public static ArrayList<Facility> getFacilityData() {
      ArrayList<Facility> Facilitylistreceive = null;
      try{
         Facilitylistreceive = (ArrayList<Facility>) FileIO.readObject("facilities.txt");
         }
         catch (ClassNotFoundException | IOException e) {
             System.out.println("File is missing. Please try again");
         }
         return Facilitylistreceive;
   }

   public static void storeFacilityData(ArrayList<Facility> fac) {
      try {
         ObjectOutputStream oos = getObjectOutputStream("facilities.txt");
         oos.writeObject(fac);
      } catch (IOException e) {			
         System.out.println(e.getMessage());
      }
   }
   public static ArrayList<Booking> getBookingData() {
      ArrayList<Booking> Bookingslistreceive = null;
      try{
         Bookingslistreceive = (ArrayList<Booking>) FileIO.readObject("bookings.txt");
         }
         catch (ClassNotFoundException | IOException e) {
             System.out.println("File is missing. Please try again");
         }
         return Bookingslistreceive;
   }

   public static void storeBookingData(ArrayList<Booking> booking) {
      try {
         ObjectOutputStream oos = getObjectOutputStream("bookings.txt");
         oos.writeObject(booking);
      } catch (IOException e) {			
         System.out.println(e.getMessage());
      }
   }

   
}
