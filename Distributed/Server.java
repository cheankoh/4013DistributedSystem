package Distributed;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Controller.FileIO;
import java.io.*;

import Model.Facility;
import Model.Booking;
import Controller.FacilityController;

public class Server {
  // Server UDP socket runs at this port
  // Make sure this port is constant with final statics

  public final static int SERVICE_PORT = 50001;

  public static void main(String[] args) throws IOException {
   
    while (true) {
      try {
     
        // Instantiate a new DatagramSocket to receive responses from the client
        // Open a UDP socket at the above service port
        DatagramSocket serverSocket = new DatagramSocket(SERVICE_PORT);
  
        /*
         * Create buffers to hold sending and receiving data. It temporarily stores data
         * in case of communication delays
         */
        byte[] receivingDataBuffer = new byte[1024];
        byte[] sendingDataBuffer = new byte[1024];
  
        /*
         * Instantiate a UDP packet to store the client data using the buffer for
         * receiving data
         */
        DatagramPacket inputPacket = new DatagramPacket(receivingDataBuffer, receivingDataBuffer.length);
        System.out.println("Waiting for a client to connect...");
  
        // Receive data from the client and store in inputPacket
        serverSocket.receive(inputPacket);
  
        // Printing out the client sent data
        String receivedData = new String(inputPacket.getData());
        System.out.println("Sent from the client: " + receivedData);
  
        // Get the facility data from facilities.txt
        ArrayList<Facility> Facilitylist = new ArrayList<Facility>();
        ArrayList<Booking> Bookinglist = new ArrayList<Booking>();

        //Initiate variable
        int facilityTypeId;
        int facilitySelection;
        int numDaysHead;
        int numDaysTail;
        int startTime;
        int endTime;
        int dayofWeek;
        int userID;
        int bookingId;
        int offset;
        int noOfSlots;
        Facilitylist = FileIO.getFacilityData();
        Bookinglist = FileIO.getBookingData();

        String sendString = "";
        // requestID = number that user chose for operation.
        int requestId = Character.getNumericValue(receivedData.charAt(0));
        switch (requestId) {
        case 1: // 1. Check Facility Availibility.
  
          facilityTypeId = Character.getNumericValue(receivedData.charAt(1));
          facilitySelection = Character.getNumericValue(receivedData.charAt(2));
  
          List<Integer> dayOfWeek = new ArrayList<Integer>();
          numDaysHead = Character.getNumericValue(receivedData.charAt(3));
          dayOfWeek.add(numDaysHead);
          numDaysTail = Character.getNumericValue(receivedData.charAt(4));
          dayOfWeek.add(numDaysTail);
  
          // facilitySelection = 1 :First facility in the respective facility type
  
          List<Integer[][]> Timeslots = FacilityController.queryAvailability(dayOfWeek, facilityTypeId, facilitySelection,
              Facilitylist);
          // List of relevent day:timeslots
  
          for (Integer[][] slots : Timeslots) {
            for (Integer[] slot : slots) {
              if (slot[0] == 0)
                sendString = sendString.concat(slot[1] + "\n");
            }
          }
  
          // Create String to send back to user.
          // for (Facility i: filteredFacilityList){
          // sendString = sendString.concat(i.getFacilityName()+ "\n");
          // }
          break;
  
        case 2:
        facilityTypeId = Character.getNumericValue(receivedData.charAt(1));
        facilitySelection = Character.getNumericValue(receivedData.charAt(2));
        dayofWeek = Character.getNumericValue(receivedData.charAt(3));
        startTime = Character.getNumericValue(receivedData.charAt(4));
        endTime = Character.getNumericValue(receivedData.charAt(5));
        userID = Character.getNumericValue(receivedData.charAt(6));
    
        System.out.println("facilityTypeId: "+ facilityTypeId);
        System.out.println("facilitySelection: "+ facilitySelection);
        System.out.println("dayofWeek: "+ dayofWeek);
        System.out.println("startTime: "+ startTime);
        System.out.println("endTime: "+ endTime);
        System.out.println("userID: "+ userID);
  
  
        int[] res = FacilityController.bookFacility(Facilitylist,facilityTypeId,facilitySelection,dayofWeek,startTime,endTime,userID);
        System.out.println("res: "+ res);

        if(res[0]==1){
          int id = res[1];
          sendString = "Booking Succesful.\n Booking ID: "+ id + ". Please remember your BookingID to update/delete" ;
        }else if(res[0]==2){
          sendString = "Booking Failed: Wrong ID";
        }else{
          sendString = "Booking Failed: Slot not available";
        }
      
        break;
        // res = 1 (Booking Succesful)
        // res = 2 (Booking Failed: Wrong ID)
        // res = 3 (Booking Failed: Slot not available)
  
  
        case 3:
        bookingId = Character.getNumericValue(receivedData.charAt(1));
        offset = Character.getNumericValue(receivedData.charAt(2));
        int[] shiftRes = FacilityController.shiftBookingSlot(bookingId,offset,Facilitylist); 

        if (shiftRes[0] == 1){
          sendString = "Booking Change Succesful. \n New Booking ID: "+ shiftRes[1] + ". Please remember your New BookingID to update/delete";
        }else if (shiftRes[0] ==-1){
          sendString = "Invalid bookingID";
        
        }else if (shiftRes[0] ==-2){
          sendString = "Booking Shift failed: Timeslot already booked";
        
        }else {
          sendString = "Booking Shift failed: Invalid offset";
        }
        break;
           // 3. Change Booking Slot
  
        case 4:
          ; // 4. Monitor Facility Availibility
            // callback
  
        case 5:
        // 5. Cancel Booking
        bookingId = Character.getNumericValue(receivedData.charAt(1));
        int deleteRes = FacilityController.cancelBooking(bookingId,Facilitylist); 

        if (deleteRes == 1){
          sendString = "Booking Delete Success";
        }else{
          sendString = "Booking Delete Failed";
        }
        break;
         

        
        case 6:
           // 6. Extend Booking Slot
          bookingId = Character.getNumericValue(receivedData.charAt(1));
          noOfSlots = Character.getNumericValue(receivedData.charAt(2));
          int[] extendRes = FacilityController.extendBookingSlot(bookingId,noOfSlots,Facilitylist); 
  
          if (extendRes[0] == 1){
            sendString = "Booking extended/shortened Succesful. \n New Booking ID: "+ extendRes[1] + ". Please remember your New BookingID to update/delete";
          }else if (extendRes[0] ==-1){
            sendString = "Invalid bookingID";
          
          }else if (extendRes[0] ==-2){
            sendString = "Booking extend/shorten failed: Timeslot already booked";
          
          }else if (extendRes[0] ==-3){
            sendString = "Booking extend/shorten failed: Invalid offset, latest slot reached";
          }else{
            sendString = "Booking extend/shorten failed: Invalid offset, minimum timeslot length";
          }
          break;
  
        case 7:
          ; // 7. Quit Program.
  
        default:
          System.out.println("Invald option!");
  
        }
  
        /*
         * Convert client sent data string to upper case, Convert it to bytes and store
         * it in the corresponding buffer.
         */
        // sendingDataBuffer = receivedData.toUpperCase().getBytes();
  
        sendingDataBuffer = sendString.getBytes();
  
        // Obtain client's IP address and the port
        InetAddress senderAddress = inputPacket.getAddress();
        int senderPort = inputPacket.getPort();
  
        // Create new UDP packet with data to send to the client
        DatagramPacket outputPacket = new DatagramPacket(sendingDataBuffer, sendingDataBuffer.length, senderAddress,
            senderPort);
  
        // Send the created packet to client
        // serverSocket.send(outputPacket);
        serverSocket.send(outputPacket);
        // Close the socket connection
        serverSocket.close();
      } catch (SocketException e) {
        e.printStackTrace();
      }
    }
     
  }
}
