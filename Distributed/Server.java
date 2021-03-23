package Distributed;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


import Controller.FileIO;
import java.io.*;

import Model.Facility;
import Model.Booking;
import Controller.FacilityController;

public class Server {
  /* Attributes for server */
  private DatagramSocket serverSocket;
  private static final int servicePort = 50001;
  // Note: Port and IP will be from client message datagram

  /* invocation semantic, if True at most once, otherwise
  assumed at least once used*/
  private boolean atMostOnce;
  /* Error simulation */
  private double failRate;
  private boolean simulateFail;

  Random rand = new Random();

  /*Constructors for Server*/
  public Server(boolean isAtMostOnceSemantic, boolean simulateFail, double failRate) throws SocketException, UnknownHostException
    {
        this.serverSocket = new DatagramSocket(servicePort);
        this.atMostOnce = isAtMostOnceSemantic;
        this.simulateFail = simulateFail;
        if (simulateFail){
            this.failRate = failRate;
        }
        else{
            this.failRate = 0.0;
        }
    }

    // Simplified constructor //
    public Server() throws SocketException, UnknownHostException
    {
        this.serverSocket = new DatagramSocket(servicePort);
        // Default invocation semantics is at least once
        this.atMostOnce = false;
        //Default error simulation - error free
        this.simulateFail = false;
        this.failRate = 0.0;
    }


    //Sending a MARSHALLED byte array to client/server
    public void send(byte[] message, InetAddress targetIP, int targetPort) throws IOException
    {
        //If error and then don't bother sending anything
        double randomProb = rand.nextDouble();
        if (this.simulateFail && randomProb <= this.failRate){
            System.out.println("[INFO][SIMULATING DROPPING OF REPLY]");
            return;
        }
        //Create a datagram packet before sending
        DatagramPacket sendingPacket = new DatagramPacket(message, message.length, targetIP, targetPort);
        this.serverSocket.send(sendingPacket);
        System.out.println("[INFO][SENT A MESSAGE TO "+ targetIP + ":" + targetPort + " ]");
    }

    //TODO: Receiving a MARSHALLED byte array over the UDP network
    //public byte[] receive() throws IOException

    public DatagramPacket receive() throws IOException
    {
        // TODO: How to determine what is the length of the receiving packet        
        // Hard code first - Best practice is to keep to one UDP and assume max possible size (waste still better)
        byte[] messageBuffer = new byte[1024];
        DatagramPacket receivingPacket = new DatagramPacket(messageBuffer, messageBuffer.length);
        
        //Receive
        serverSocket.receive(receivingPacket);
        System.out.println("[INFO][RECEIVED REQUEST" + receivingPacket.getAddress() + ":" + receivingPacket.getPort() + "]");
        
        //return messageBuffer; 

        //TODO: Temporary
        return receivingPacket;
    }
    
    //TODO Higher receive - process - send reply routine
    //Extracting information
    // InetAddress sourceIP = receivingPacket.getAddress();
    // int sourcePort = receivingPacket.getPort();
  
  public static void main(String[] args) throws IOException {
    //TODO: Do this as command line if possible
    boolean atMostOnce = false;
    boolean simulateFail = true;
    double probFailure = 0.2;
    
    //Set up constructor
    Server server = new Server(atMostOnce,simulateFail,probFailure);
    System.out.println("[INFO][SERVER INITIATED]");

    while (true) {
      try {
     
        System.out.println("[INFO][WAITING FOR REQUEST...]");
        DatagramPacket inputPacket = server.receive();

        //TODO Check the format of send - received
  
        // Printing out the client sent data
        String receivedData = new String(inputPacket.getData());
        System.out.println("Sent from the client: " + receivedData);


        //TODO: Find a better way to embedd the application interface
  
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
        byte[] sendingDataBuffer = new byte[1024];
        sendingDataBuffer = sendString.getBytes();
  
        // Obtain client's IP address and the port
        InetAddress senderAddress = inputPacket.getAddress();
        int senderPort = inputPacket.getPort();
  
        // Create new UDP packet with data to send to the client
  
        // Send the created packet to client
        // serverSocket.send(outputPacket);
        server.send(sendingDataBuffer,senderAddress,senderPort);
      } 
      catch (SocketException e) {
        e.printStackTrace();
      }
    }
     
  }
}
