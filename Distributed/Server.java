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
        
        //Initiate variable
        int facilityTypeId;
        int facilitySelection;
        int numDaysHead;
        int numDaysTail;
        int startTime;
        int endTime;
        int dayofWeek;
        int userID;
  
        Facilitylist = FileIO.getFacilityData();
  
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
  
  
        int res = FacilityController.bookFacility(Facilitylist,facilityTypeId,facilitySelection,dayofWeek,startTime,endTime,userID);
        System.out.println("res: "+ res);

        if(res==1){
          sendString = "Booking Succesful";
        }else if(res==2){
          sendString = "Booking Failed: Wrong ID";
        }else{
          sendString = "Booking Failed: Slot not available";
        }
      
        break;
        // res = 1 (Booking Succesful)
        // res = 2 (Booking Failed: Wrong ID)
        // res = 3 (Booking Failed: Slot not available)
  
  
        case 3:
          ; // 3. Change Booking Slot
  
        case 4:
          ; // 4. Monitor Facility Availibility
  
        case 5:
          ; // 5. Cancel Booking
  
        case 6:
          ; // 6. Extend Booking Slot
  
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
