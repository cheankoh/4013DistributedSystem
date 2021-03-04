package Distributed;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

import Controller.FileIO;
import java.io.*;

import Model.Facility;

public class Server{
  // Server UDP socket runs at this port
  // Make sure this port is constant with final statics
  public final static int SERVICE_PORT=50001;
private static Integer[][] integers;
 
  public static void main(String[] args) throws IOException{
    try{
      // Instantiate a new DatagramSocket to receive responses from the client
      // Open a UDP socket at the above service port
      DatagramSocket serverSocket = new DatagramSocket(SERVICE_PORT);
      
      /* Create buffers to hold sending and receiving data.
      It temporarily stores data in case of communication delays */
      byte[] receivingDataBuffer = new byte[1024];
      byte[] sendingDataBuffer = new byte[1024];
      
      /* Instantiate a UDP packet to store the 
      client data using the buffer for receiving data*/
      DatagramPacket inputPacket = new DatagramPacket(receivingDataBuffer, receivingDataBuffer.length);
      System.out.println("Waiting for a client to connect...");
      
      // Receive data from the client and store in inputPacket
      serverSocket.receive(inputPacket);
      
      // Printing out the client sent data
      String receivedData = new String(inputPacket.getData());
      System.out.println("Sent from the client: "+receivedData);
      

      //Get the facility data from facilities.txt
      ArrayList<Facility> Facilitylistreceive= new ArrayList<Facility>();
      try{
        Facilitylistreceive = (ArrayList<Facility>) FileIO.readObject("facilities.txt");
        }
        catch (ClassNotFoundException | IOException e) {
            System.out.println("File is missing. Please try again");
        }

      String sendString = "";
      //requestID = number that user chose for operation.
      int requestId = Character.getNumericValue(receivedData.charAt(0));
      switch(requestId){
        case 1: //1. Check Facility Availibility.

          int facilityTypeId = Character.getNumericValue(receivedData.charAt(1));
          int facilitySelection = Character.getNumericValue(receivedData.charAt(2));
          int numDays = Character.getNumericValue(receivedData.charAt(3));
      //Filter and create a new arraylist to filter and store relevent facilities
      ArrayList<Facility> filteredFacilityList= new ArrayList<Facility>();
      for (Facility i: Facilitylistreceive){
        if((i.getFacilityType()==facilityTypeId)){
          filteredFacilityList.add(i);
        }
      }
      
      Facility targetFacility = filteredFacilityList.get(facilitySelection-1); 
      //facilitySelection = 1 :First facility in the respective facility type

      HashMap<Integer, Integer[][]> availability = targetFacility.getAvailability();
      Integer[][] timeslot = availability.get(numDays);
      for (Integer[] slot: timeslot){
        if (slot[0] == 0)
        sendString = sendString.concat(slot[1]+ "\n");
       }
      //Create String to send back to user.
      // for (Facility i: filteredFacilityList){
      //   sendString = sendString.concat(i.getFacilityName()+ "\n");
      //  }
       break;

       case 2:; //2. Book Facility.
          
       case 3:; //3. Change Booking Slot

       case 4:; //4. Monitor Facility Availibility

       case 5:; //5. Cancel Booking

       case 6:; //6. Extend Booking Slot

       case 7:; //7. Quit Program.

       default:
          System.out.println("Invald option!");

      }



  
      


      /* 
      * Convert client sent data string to upper case,
      * Convert it to bytes
      *  and store it in the corresponding buffer. */
      // sendingDataBuffer = receivedData.toUpperCase().getBytes();


      sendingDataBuffer = sendString.getBytes();
      
      // Obtain client's IP address and the port
      InetAddress senderAddress = inputPacket.getAddress();
      int senderPort = inputPacket.getPort();
      
      // Create new UDP packet with data to send to the client
      DatagramPacket outputPacket = new DatagramPacket(
        sendingDataBuffer, sendingDataBuffer.length,
        senderAddress,senderPort
      );
      
      // Send the created packet to client
     // serverSocket.send(outputPacket);
      serverSocket.send(outputPacket);
      // Close the socket connection
      serverSocket.close();
    }
    catch (SocketException e){
      e.printStackTrace();
    }
  }
}
