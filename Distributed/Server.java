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

import Distributed.Util;
import Distributed.HistoryKey;

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

    //Receiving a MARSHALLED byte array over the UDP network
    public DatagramPacket receive() throws IOException
    {
        byte[] messageBuffer = new byte[Util.MAX_SIZE];
        DatagramPacket receivingPacket = new DatagramPacket(messageBuffer, messageBuffer.length);
        
        //Receive
        serverSocket.receive(receivingPacket);
        System.out.println("[INFO][RECEIVED REQUEST FROM " + receivingPacket.getAddress() + ":" + receivingPacket.getPort() + "]");
        
        return receivingPacket;
    }
    
    //TODO Higher receive - process - send reply routine
    //Extracting information
    // InetAddress sourceIP = receivingPacket.getAddress();
    // int sourcePort = receivingPacket.getPort();
  
  public static void main(String[] args) throws IOException {
    //TODO: Do this as command line if possible
    boolean atMostOnce = true;
    boolean simulateFail = true;
    double probFailure = 0.8;

    //About payload to create a reply message
    byte communicationMethod;
    byte replyType;
    int messageID;
    byte[] payload;
    int payloadSize;
    
    //Set up constructor
    Server server = new Server(atMostOnce,simulateFail,probFailure);
    System.out.println("[INFO][SERVER INITIATED]");

    //History if atMostOnce semantic is true
    //This store IP/ Port in string as key and Array
    HashMap<HistoryKey,HashMap<Integer,byte[]>> historyMap = null;
    if (atMostOnce){
      System.out.println("[INFO][AT MOST ONCE SEMANTICS USED]");
      historyMap = new HashMap<HistoryKey,HashMap<Integer,byte[]>>();
    }
    else{
      System.out.println("[INFO][AT LEAST ONCE SEMANTICS USED]");
    }

    while (true) {
      try {
     
        System.out.println("[INFO][WAITING FOR REQUEST...]");
        DatagramPacket received = server.receive();
        byte[] inputPacket = received.getData();

        // Obtain client's IP address and the port
        InetAddress clientAddress = received.getAddress();
        int clientPort = received.getPort();
  
        //client sent data
        byte clientCommMethod = Util.getCommMethod(inputPacket);
        byte clientMsgType = Util.getMsgType(inputPacket);
        int  clientMsgID   = Util.getMsgID(inputPacket);
        int  clientPayloadSize   = Util.getPayloadSize(inputPacket);
        byte[] clientPayload     = Util.getPayload(inputPacket);

        //Display for debug purpose
        System.out.println("[DEBUG][SENT FROM CLIENT - METHOD: " + clientCommMethod + ", MESS_TYPE: "
        + clientMsgType + ", MESS_ID: " + clientMsgID + ", SIZE: " + clientPayloadSize + ", DATA: " + clientPayload.toString());

        //TODO: Find a better way to embedd the application interface
        //TODO: Check and ignore non request

        //Check history if AT MOST ONCE
        if(atMostOnce){
          System.out.println("[DEBUG][CHECKING REQUEST HISTORY FOR DUPLICATES]");
          HistoryKey key = new HistoryKey(clientAddress.toString(),clientPort);
          if (historyMap.containsKey(key)){
            System.out.println("[DEBUG][CLIENT IP & PORT FOUND. CHECKING REQUEST ID]");
            if (historyMap.get(key).containsKey(clientMsgID)){
              System.out.println("[DEBUG][DUPLICATE REQUEST ID FOUND. RETRANSMITTING...]");
              // Just retransmit the message directly from history
              server.send(historyMap.get(key).get(clientMsgID),clientAddress,clientPort);
              continue;
            }
          }
        }
  
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

        communicationMethod = 2;
        replyType = clientMsgType;
        messageID = 0;

        switch (clientMsgType) {
        case 1: // 1. Check Facility Availibility.
  
          facilityTypeId = Util.getFacilityType(clientPayload);
          facilitySelection = Util.getFacilityNum(clientPayload);
  
          List<Integer> dayOfWeek = new ArrayList<Integer>();
          numDaysHead = Util.getDate(clientPayload).charAt(0);
          dayOfWeek.add(numDaysHead);
          numDaysTail = Util.getDate(clientPayload).charAt(1);
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
        facilityTypeId = Util.getFacilityType(clientPayload);
        facilitySelection = Util.getFacilityNum(clientPayload);
        dayofWeek = Util.getDayOfWeek(clientPayload);
        startTime = Util.getStartSlot(clientPayload);
        endTime = Util.getStopSlot(clientPayload);
        userID = Util.getUserID(clientPayload);
    
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
          replyType = 0;
          sendString = "Booking Failed: Wrong ID";
        }else{
          replyType = 0;
          sendString = "Booking Failed: Slot not available";
        }
      
        break;
        // res = 1 (Booking Succesful)
        // res = 2 (Booking Failed: Wrong ID)
        // res = 3 (Booking Failed: Slot not available)
  
  
        case 3:
        bookingId = Util.getBookingID(clientPayload);
        offset = Util.getOffset(clientPayload);
        int[] shiftRes = FacilityController.shiftBookingSlot(bookingId,offset,Facilitylist); 

        if (shiftRes[0] == 1){
          sendString = "Booking Change Succesful. \n New Booking ID: "+ shiftRes[1] + ". Please remember your New BookingID to update/delete";
        }else if (shiftRes[0] ==-1){
          replyType = 0;
          sendString = "Invalid bookingID";
        
        }else if (shiftRes[0] ==-2){
          replyType = 0;
          sendString = "Booking Shift failed: Timeslot already booked";
        
        }else {
          replyType = 0;
          sendString = "Booking Shift failed: Invalid offset";
        }
        break;
           // 3. Change Booking Slot
  
        case 4:
          ; // 4. Monitor Facility Availibility
            // callback
  
        case 5:
        // 5. Cancel Booking
        bookingId = Util.getBookingID(clientPayload);
        int deleteRes = FacilityController.cancelBooking(bookingId,Facilitylist); 

        if (deleteRes == 1){
          sendString = "Booking Delete Success";
        }else{
          replyType = 0;
          sendString = "Booking Delete Failed";
        }
        break;
         

        
        case 6:
           // 6. Extend Booking Slot
          bookingId = Util.getBookingID(clientPayload);
          noOfSlots = Util.getOffset(clientPayload);
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
  
        payload = Util.marshall(sendString);
        payloadSize = Util.marshall(sendString).length;

        // Create new UDP packet with data to send to the client
        byte[] sendBuffer = Util.getMessageByte(communicationMethod, replyType, messageID, payloadSize, payload);
        
        // If AT MOST ONCE, save to history before sending
        if(atMostOnce){
          System.out.println("[DEBUG][INSERTING INTO HISTORY]");
          HistoryKey savedKey = new HistoryKey(clientAddress.toString(), clientPort);
          if (historyMap.containsKey(savedKey)){
            historyMap.get(savedKey).put(messageID, sendBuffer);
            System.out.println("[DEBUG][INSERTED NEW REQUEST ID]");
          }
          else{
            historyMap.put(savedKey,(new HashMap<Integer,byte[]>()));
            historyMap.get(savedKey).put(messageID, sendBuffer);
            System.out.println("[DEBUG][INSERTED NEW IP, PORT AND REQUEST ID]");
          }
        }

        // Send the created packet to client
        // serverSocket.send(outputPacket);
        server.send(sendBuffer,clientAddress,clientPort);
      } 
      catch (SocketException e) {
        e.printStackTrace();
      }
    }
     
  }
}
