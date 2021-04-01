package Distributed;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import java.io.*;

import Model.Facility;
import jdk.nashorn.internal.codegen.CompilerConstants.Call;
import Model.Booking;
import Controller.DatabaseConnection;
import Controller.FacilityController;

import Distributed.Util;
import Distributed.HistoryKey;
import Distributed.CallbackHistoryKey;

public class Server {
  /* Attributes for server */
  private DatagramSocket serverSocket;
  private static final int servicePort = 50001;
  // Note: Port and IP will be from client message datagram

  /*
   * invocation semantic, if True at most once, otherwise assumed at least once
   * used
   */
  private boolean atMostOnce;
  /* Error simulation */
  private double failRate;
  private boolean simulateFail;

  Random rand = new Random();

  /* Constructors for Server */
  public Server(boolean isAtMostOnceSemantic, boolean simulateFail, double failRate)
      throws SocketException, UnknownHostException {
    this.serverSocket = new DatagramSocket(servicePort);
    this.atMostOnce = isAtMostOnceSemantic;
    this.simulateFail = simulateFail;
    if (simulateFail) {
      this.failRate = failRate;
    } else {
      this.failRate = 0.0;
    }
  }

  // Simplified constructor //
  public Server() throws SocketException, UnknownHostException {
    this.serverSocket = new DatagramSocket(servicePort);
    // Default invocation semantics is at least once
    this.atMostOnce = false;
    // Default error simulation - error free
    this.simulateFail = false;
    this.failRate = 0.0;
  }

  // Sending a MARSHALLED byte array to client/server
  public void send(byte[] message, InetAddress targetIP, int targetPort) throws IOException {
    // If error and then don't bother sending anything
    double randomProb = rand.nextDouble();
    if (this.simulateFail && randomProb <= this.failRate) {
      System.out.println("[INFO][SIMULATING DROPPING OF REPLY]");
      return;
    }
    // Create a datagram packet before sending
    DatagramPacket sendingPacket = new DatagramPacket(message, message.length, targetIP, targetPort);
    this.serverSocket.send(sendingPacket);
    System.out.println("[INFO][SENT A MESSAGE TO " + targetIP + ":" + targetPort + " ]");
  }

  // Receiving a MARSHALLED byte array over the UDP network
  public DatagramPacket receive() throws IOException {
    byte[] messageBuffer = new byte[Util.NORMAL_SIZE];
    DatagramPacket receivingPacket = new DatagramPacket(messageBuffer, messageBuffer.length);

    // Receive
    serverSocket.receive(receivingPacket);
    System.out.println(
        "[INFO][RECEIVED REQUEST FROM " + receivingPacket.getAddress() + ":" + receivingPacket.getPort() + "]");

    return receivingPacket;
  }

  // TODO Higher receive - process - send reply routine
  // Extracting information
  // InetAddress sourceIP = receivingPacket.getAddress();
  // int sourcePort = receivingPacket.getPort();

  public static void main(String[] args) throws IOException {
    // TODO: Do this as command line if possible
    boolean atMostOnce = false;
    boolean simulateFail = true;
    double probFailure = 0.5;

    // About payload to create a reply message
    byte communicationMethod;
    byte replyType;
    int messageID;
    byte[] payload;
    int payloadSize;

    // Set up constructor
    Server server = new Server(atMostOnce, simulateFail, probFailure);
    System.out.println("[INFO][SERVER INITIATED]");

    // History if atMostOnce semantic is true
    // This store IP/ Port in string as key and Array
    HashMap<HistoryKey, HashMap<Integer, byte[]>> historyMap = null;

    // History of callback
    // This store IP/ Port in string as key and Array
    HashMap<CallbackHistoryKey, HashMap<Integer, Long>> cbHistory = new HashMap<CallbackHistoryKey, HashMap<Integer, Long>>();
    if (atMostOnce) {
      System.out.println("[INFO][AT MOST ONCE SEMANTICS USED]");
      historyMap = new HashMap<HistoryKey, HashMap<Integer, byte[]>>();
    } else {
      System.out.println("[INFO][AT LEAST ONCE SEMANTICS USED]");
    }

    while (true) {
      try {
        DatabaseConnection db = new DatabaseConnection();
        System.out.println("[INFO][MYSQL CONNECTION SUCCESSFULLY ESTABLISHED...]");
        System.out.println("[INFO][WAITING FOR REQUEST...]");
        DatagramPacket received = server.receive();
        byte[] inputPacket = received.getData();

        // Obtain client's IP address and the port
        InetAddress clientAddress = received.getAddress();
        int clientPort = received.getPort();

        // client sent data
        byte clientCommMethod = Util.getCommMethod(inputPacket);
        byte clientMsgType = Util.getMsgType(inputPacket);
        int clientMsgID = Util.getMsgID(inputPacket);
        int clientPayloadSize = Util.getPayloadSize(inputPacket);
        byte[] clientPayload = Util.getPayload(inputPacket);

        // Display for debug purpose
        System.out.println("[DEBUG][SENT FROM CLIENT - METHOD: " + clientCommMethod + ", MESS_TYPE: " + clientMsgType
            + ", MESS_ID: " + clientMsgID + ", SIZE: " + clientPayloadSize + ", DATA: "
            + Util.encodeHexString(clientPayload) + "]");

        // TODO: Find a better way to embedd the application interface
        // TODO: Check and ignore non request

        // Check history if AT MOST ONCE
        if (atMostOnce) {
          System.out.println("[DEBUG][CHECKING REQUEST HISTORY FOR DUPLICATES]");
          HistoryKey key = new HistoryKey(clientAddress.toString(), clientPort);
          if (historyMap.containsKey(key)) {
            System.out.println("[DEBUG][CLIENT IP & PORT FOUND. CHECKING REQUEST ID]");
            if (historyMap.get(key).containsKey(clientMsgID)) {
              System.out.println("[DEBUG][DUPLICATE REQUEST ID FOUND. RETRANSMITTING...]");
              // Just retransmit the message directly from history
              server.send(historyMap.get(key).get(clientMsgID), clientAddress, clientPort);
              System.out.println();
              continue;
            }
          }
        }

        // Get the facility data from facilities.txt
        ArrayList<Facility> Facilitylist = new ArrayList<Facility>();
        ArrayList<Booking> Bookinglist = new ArrayList<Booking>();

        // Initiate variable
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
        Facilitylist = db.getFacilityList(); //Get facility list from database
        System.out.println("Facility Size:" + Facilitylist.size());
        Bookinglist = db.getBookingList(); //Get booking list from database
        String sendString = "";
        String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

        communicationMethod = 2;
        replyType = clientMsgType;
        messageID = 0;

        switch (clientMsgType) {
        case 1: // 1. Check Facility Availibility.

          facilityTypeId = Util.getFacilityType(clientPayload);
          facilitySelection = Util.getFacilityNum(clientPayload);

          List<Integer> dayOfWeek = new ArrayList<Integer>();

          String date = Util.getDate(clientPayload, clientPayloadSize);
          numDaysHead = date.charAt(0) - 48;
          dayOfWeek.add(numDaysHead);
          System.out.println(numDaysHead);
          try {
            numDaysTail = date.charAt(1) - 48;
          } catch (Exception e) {
            // TODO: handle exception
            numDaysTail = -1;
          }
          dayOfWeek.add(numDaysTail);
          System.out.println(numDaysTail);

          // facilitySelection = 1 :First facility in the respective facility type

          List<Integer[][]> Timeslots = FacilityController.queryAvailability(dayOfWeek, facilityTypeId,
              facilitySelection, Facilitylist);
          // List of relevent day:timeslots
          if (Timeslots == null) {
            sendString = "invalid day entered";
          } else {
            int head = dayOfWeek.get(0);
            for (Integer[][] slots : Timeslots) {
              sendString = sendString.concat(days[head - 1] + "\n");
              sendString = sendString.concat("######################" + "\n");
              head++;
              for (Integer[] slot : slots) {
                if (slot[0] == 0) {
                  sendString = sendString.concat(slot[1] + "\n");
                }
              }
            }

          }

          System.out.println("sendString: " + sendString);
       
          break;

        case 2: // 2. Book Facility.
          facilityTypeId = Util.getFacilityType(clientPayload);
          facilitySelection = Util.getFacilityNum(clientPayload);
          dayofWeek = Util.getDayOfWeek(clientPayload);
          startTime = Util.getStartSlot(clientPayload);
          endTime = Util.getStopSlot(clientPayload);
          userID = Util.getUserID(clientPayload);

          System.out.println("facilityTypeId: " + facilityTypeId);
          System.out.println("facilitySelection: " + facilitySelection);
          System.out.println("dayofWeek: " + dayofWeek);
          System.out.println("startTime: " + startTime);
          System.out.println("endTime: " + endTime);
          System.out.println("userID: " + userID);

          int[] res = FacilityController.bookFacility(Facilitylist, facilityTypeId, facilitySelection, dayofWeek,
              startTime, endTime, userID, db);
          System.out.println("res: " + res);

          if (res[0] == 1) {
            int id = res[1];
            sendString = "Booking Successful.\n Booking ID: " + id
                + ". Please remember your BookingID to update/delete";
            List<Integer> temp = new ArrayList<Integer>();
            temp.add(dayofWeek);
            temp.add(-1);
            List<Integer[][]> callbackAvail = FacilityController.queryAvailability(temp, facilityTypeId,
                facilitySelection, Facilitylist);
            String callbackString = "";
            if (callbackAvail == null) {
              callbackString = "invalid day entered";
            } else {
              int head = temp.get(0);
              for (Integer[][] slots : callbackAvail) {
                callbackString = callbackString.concat(days[head - 1] + "\n");
                callbackString = callbackString.concat("######################" + "\n");
                head++;
                for (Integer[] slot : slots) {
                  if (slot[0] == 0) {
                    System.out.println("SLOT[0]" + slot[0]);
                    callbackString = callbackString.concat(slot[1] + "\n");
                  }
                }
              }

            }
            callbackHandler(cbHistory, facilitySelection + (2 * (facilityTypeId - 1)), callbackString, server,
                communicationMethod, replyType, messageID);
          } else if (res[0] == 0) {
            replyType = 0;
            sendString = "Failed to create booking";
          } else if (res[0] == -1) {
            replyType = 0;
            sendString = "Booking Failed: Invalid selection for facility";
          } else if (res[0] == -2) {
            replyType = 0;
            sendString = "Booking Failed: Invalid start/end time";
          } else if (res[0] == -3) {
            replyType = 0;
            sendString = "Booking Failed:Timeslot already booked";
          } else {
            replyType = 0;
            sendString = "Unidentified error";
          }

          break;

        case 3: // 3. Change Facility Booking Slot.
          bookingId = Util.getBookingID(clientPayload);
          offset = Util.getOffset(clientPayload);
          int[] shiftRes = FacilityController.shiftBookingSlot(bookingId, offset, Facilitylist, db);

          if (shiftRes[0] == 1) {
            sendString = "Booking Change Succesful. \n New Booking ID: " + shiftRes[1]
                + ". Please remember your New BookingID to update/delete";
            List<Integer> temp = new ArrayList<Integer>();
            temp.add(shiftRes[3]);
            temp.add(-1);
            List<Integer[][]> callbackAvail = FacilityController.queryAvailability(temp, shiftRes[4],
                shiftRes[2] - (2 * (shiftRes[4]) - 1), Facilitylist);
            String callbackString = "";
            for (Integer[][] slots : callbackAvail) {
              for (Integer[] slot : slots) {
                if (slot[0] == 0)
                  callbackString = callbackString.concat(slot[1] + "\n");
              }
            }
            callbackHandler(cbHistory, shiftRes[2], callbackString, server, communicationMethod, replyType, messageID);
          } else if (shiftRes[0] == -1) {
            replyType = 0;
            sendString = "Invalid bookingID";

          } else if (shiftRes[0] == -2) {
            replyType = 0;
            sendString = "Booking Shift failed: Timeslot already booked";

          } else if (shiftRes[0] == -3) {
            replyType = 0;
            sendString = "Booking Shift failed: Invalid offset";
          } else {
            replyType = 0;
            sendString = "Unidentified error";
          }
          break;
        // 3. Change Booking Slot

        case 4:
          // 4. Monitor Facility Availibility
          int typeID = Util.getFacilityType(clientPayload);
          int facilityID = Util.getFacilityNum(clientPayload) + (2 * (typeID - 1));
          int duration = Util.getDuration(clientPayload);
          System.out.println("[DEBUG][INSERTING INTO CALLBACK]");
          CallbackHistoryKey savedKey = new CallbackHistoryKey(clientAddress, clientPort);

          // Store the client ID (ip&port) and the period of callback
          long serverTime = System.currentTimeMillis() + (Long.valueOf(duration) * 1000);
          Long t3 = serverTime - (Long.valueOf(duration) * 1000); // Server time when finish processing
          System.out.println();
          if (cbHistory.containsKey(savedKey)) {
            System.out.println("[INFO][CLIENT HAS REGISTERED BEFORE]");
            if (atMostOnce) { // at most once semantic is used
              t3 = cbHistory.get(savedKey).get(facilityID) - Long.valueOf(duration) * 1000;
              System.out.println("[INFO][ATMOSTONCE IS USED THUS RETURNING STORED SERVER TIME]");
            } else { // at least once semantic is used
              cbHistory.remove(savedKey);

              System.out.println("[DEBUG][SavedKey EXISTS : " + cbHistory.containsKey(savedKey) + "]");
              cbHistory.put(savedKey, (new HashMap<Integer, Long>()));
              cbHistory.get(savedKey).put(facilityID, serverTime);
              System.out.println("[INFO][ATLEASTONCE IS USED THUS RETURNING NEW SERVER TIME]");
            }
          } else {
            cbHistory.put(savedKey, (new HashMap<Integer, Long>()));
            cbHistory.get(savedKey).put(facilityID, serverTime);
            System.out.println("[DEBUG][INSERTED NEW IP, PORT FOR CALLBACK]");
          }

          sendString = t3.toString();
          break;
        // callback

        case 5:
          // 5. Cancel Booking
          bookingId = Util.getBookingID(clientPayload);
          int[] deleteRes = FacilityController.cancelBooking(bookingId, Facilitylist, db);

          if (deleteRes[0] == 1) {
            sendString = "Booking Delete Success";
            List<Integer> temp = new ArrayList<Integer>();
            temp.add(deleteRes[2]);
            temp.add(-1);
            List<Integer[][]> callbackAvail = FacilityController.queryAvailability(temp, deleteRes[3],
                deleteRes[1] - (2 * (deleteRes[3]) - 1), Facilitylist);
            String callbackString = "";
            for (Integer[][] slots : callbackAvail) {
              for (Integer[] slot : slots) {
                if (slot[0] == 0)
                  callbackString = callbackString.concat(slot[1] + "\n");
              }
            }
            callbackHandler(cbHistory, deleteRes[1], callbackString, server, communicationMethod, replyType, messageID);
          } else if (deleteRes[0] == -1) {
            replyType = 0;
            sendString = "Booking Delete Failed";
          } else {
            replyType = 0;
            sendString = "Unidentified error";
          }
          break;

        case 6:
          // 6. Extend Booking Slot
          bookingId = Util.getBookingID(clientPayload);
          noOfSlots = Util.getOffset(clientPayload);
          int[] extendRes = FacilityController.extendBookingSlot(bookingId, noOfSlots, Facilitylist, db);

          if (extendRes[0] == 1) {
            sendString = "Booking extended/shortened Succesful. \n New Booking ID: " + extendRes[1]
                + ". Please remember your New BookingID to update/delete";
            List<Integer> temp = new ArrayList<Integer>();
            temp.add(extendRes[3]);
            temp.add(-1);
            List<Integer[][]> callbackAvail = FacilityController.queryAvailability(temp, extendRes[4],
                extendRes[2] - (2 * (extendRes[4] - 1)), Facilitylist);
            String callbackString = "";
            for (Integer[][] slots : callbackAvail) {
              for (Integer[] slot : slots) {
                if (slot[0] == 0)
                  callbackString = callbackString.concat(slot[1] + "\n");
              }
            }
            callbackHandler(cbHistory, extendRes[2], callbackString, server, communicationMethod, replyType, messageID);
          } else if (extendRes[0] == 0) {
            sendString = "Booking extend/shorten failed: Unable to create booking";
          } else if (extendRes[0] == -1) {
            sendString = "Invalid bookingID";

          } else if (extendRes[0] == -2) {
            sendString = "Booking extend/shorten failed: Timeslot already booked";

          } else if (extendRes[0] == -3) {
            sendString = "Booking extend/shorten failed: Invalid offset, latest slot reached";
          } else if (extendRes[0] == -4) {
            sendString = "Booking extend/shorten failed: Invalid offset, minimum timeslot length";
          } else {
            sendString = "Unidentified error";

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
        if (atMostOnce) {
          System.out.println("[DEBUG][INSERTING INTO HISTORY]");
          HistoryKey savedKey = new HistoryKey(clientAddress.toString(), clientPort);
          if (historyMap.containsKey(savedKey)) {
            historyMap.get(savedKey).put(clientMsgID, sendBuffer);
            System.out.println("[DEBUG][INSERTED NEW REQUEST ID]");
          } else {
            historyMap.put(savedKey, (new HashMap<Integer, byte[]>()));
            historyMap.get(savedKey).put(clientMsgID, sendBuffer);
            System.out.println("[DEBUG][INSERTED NEW IP, PORT AND REQUEST ID]");
          }
        }

        // Send the created packet to client
        // serverSocket.send(outputPacket);
        server.send(sendBuffer, clientAddress, clientPort);
        System.out.println();
      } catch (SocketException e) {
        e.printStackTrace();
      }
    }

  }

  
  public static void callbackHandler(HashMap<CallbackHistoryKey, HashMap<Integer, Long>> cbHistoryKey, int facilityID,
      String sendString, Server server, byte communicationMethod, byte replyType, int messageID) {
    if (cbHistoryKey == null)
      return;

    byte[] payload = Util.marshall(sendString);
    int payloadSize = Util.marshall(sendString).length;
    byte[] sendBuffer = Util.getMessageByte(communicationMethod, replyType, messageID, payloadSize, payload);
    ArrayList<CallbackHistoryKey> toRemove = new ArrayList<CallbackHistoryKey>();
    System.out.println("[DEBUG][ENTERED CALLBACK FUNCTION]");
    for (HashMap.Entry<CallbackHistoryKey, HashMap<Integer, Long>> callback : cbHistoryKey.entrySet()) {
      if (callback.getValue().containsKey(facilityID)) {
        System.out.println("EXPIRY TIME: " + callback.getValue().get(facilityID));
        System.out.println("CURRENT TIME: " + System.currentTimeMillis());
        boolean notClientExpired = callback.getValue().get(facilityID) > System.currentTimeMillis();
        System.out.println("[DEBUG][CLIENT NOT EXPIRED: " + notClientExpired + "]");
        if (notClientExpired) {
          try {
            server.send(sendBuffer, callback.getKey().getIPAddress(), callback.getKey().getPort());
            System.out.println("[DEBUG][SERVER HAS SENT CALLBACK MESSAGE TO CLIENT]");
          } catch (Exception e) {
            // TODO: handle exception
            System.out.println("[DEBUG][ERROR HAS OCCURRED TRYING TO SEND CALLBACK MESSAGE TO REGISTERED CLIENT]");
            e.printStackTrace();
          }
        } else {
          // remove the whole history of that client if the callback is expired
          System.out.println("[DEBUG][DELETING PARTICULAR USER]");
          toRemove.add(callback.getKey());
        }
      }
    }
    for (CallbackHistoryKey cb : toRemove) {
      cbHistoryKey.remove(cb);
    }
  }
}
