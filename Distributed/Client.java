package Distributed;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Random;
import java.util.Date;
import java.util.DateFormat;
import java.util.SimpleDateFormat;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


//JUST TO TEST
import java.nio.charset.StandardCharsets;
//Marshal-Demarshal
import Distributed.Util;

public class Client {
    /* Attributes for client */
    private DatagramSocket clientSocket;
    private InetAddress hostIP;
    private int hostPort;
    /* Request timeout in sec */
    private int requestTimeout;
    /*
     * invocation semantic, if True at most once, otherwise assumed at least once
     * used
     */
    private boolean atMostOnce;
    /* Error simulation */
    private double failRate;
    private boolean simulateFail;
    /* Keep track of message ID */
    private int messageCounter;

    Random rand = new Random();

    public Client(String hostIP, int hostPort, int timeout, boolean isAtMostOnceSemantic, boolean simulateFail,
            double failRate) throws SocketException, UnknownHostException {
        this.clientSocket = new DatagramSocket();
        this.hostIP = InetAddress.getByName(hostIP);
        this.hostPort = hostPort;
        this.requestTimeout = timeout;
        this.atMostOnce = isAtMostOnceSemantic;
        this.simulateFail = simulateFail;
        if (simulateFail) {
            this.failRate = failRate;
        } else {
            this.failRate = 0.0;
        }
        this.messageCounter = 0;
    }

    // Simplified constructor //
    public Client(String hostIP, int hostPort) throws SocketException, UnknownHostException {
        this.clientSocket = new DatagramSocket();
        this.hostIP = InetAddress.getByName(hostIP);
        this.hostPort = hostPort;
        // Default timeout is 1 sec
        this.requestTimeout = 5;
        // Default invocation semantics is at least once
        this.atMostOnce = false;
        // Default error simulation - error free
        this.simulateFail = false;
        this.failRate = 0.0;
        this.messageCounter = 0;
    }

    // TODO: Add setters for other attributes when added.
    public int getCurMsgCount() {
        return this.messageCounter;
    }

    // Sending a MARSHALLED byte array over the server
    // Return True = need to allocate max; False = no need to allocate so big
    public void send(byte[] message) throws IOException {
        // If error and then don't bother sending anything
        double randomProb = rand.nextDouble();
        if (this.simulateFail && randomProb <= this.failRate) {
            System.out.println("[INFO][SIMULATING DROPPING OF REQUEST]");
            return;
        }
        // Create a datagram packet before sending
        DatagramPacket sendingPacket = new DatagramPacket(message, message.length, this.hostIP, this.hostPort);
        this.clientSocket.send(sendingPacket);
        System.out.println("[INFO][SENT A MESSAGE TO SERVER]");
    }

    // Receiving a MARSHALLED byte array over the UDP network
    public byte[] receive(boolean isMaxSize) throws IOException, SocketTimeoutException {
        byte[] messageBuffer = new byte[Util.NORMAL_SIZE];

        if (isMaxSize)
            messageBuffer = new byte[Util.MAX_SIZE];

        DatagramPacket receivingPacket = new DatagramPacket(messageBuffer, messageBuffer.length);

        // Timeout in milliseconds
        clientSocket.setSoTimeout(this.requestTimeout * 1000);

        // Receive
        clientSocket.receive(receivingPacket);
        System.out.println("[INFO][RECEIVED REPLY BY SERVER]");

        return receivingPacket.getData();
    }

    // Receiving a MARSHALLED byte array over the UDP network
    public byte[] receiveCallback(int duration, Long t3) throws IOException {
        byte[] messageBuffer = new byte[Util.MAX_SIZE];

        DatagramPacket receivingPacket = new DatagramPacket(messageBuffer, messageBuffer.length);

        // manually set timeout longer than the while loop by 1 second
        Long timePassedLong = (System.currentTimeMillis() - t3);
        int timePassed = timePassedLong.intValue();
        this.clientSocket.setSoTimeout((duration) * 1000 - timePassed);
        // Receive
        this.clientSocket.receive(receivingPacket);
        System.out.println("[INFO][RECEIVED CALLBACK BY SERVER]");

        return receivingPacket.getData();
    }

    // Wrap send and receive together for reusibility
    public byte[] routineSendReceive(byte[] message, boolean useMaxSize) throws IOException, SocketTimeoutException {
        int numTimeouts = 0;
        int maxTimeouts = 10; // @TODO: Move to constants

        // dummy byte array of length 0
        byte[] response = new byte[0];

        // Retry if timeout up until a max value
        while (numTimeouts < maxTimeouts) {
            try {
                send(message);
                response = receive(useMaxSize);
                break;
            } catch (SocketTimeoutException e) {
                System.out.println("[ERROR][REQUEST TIMEOUT. RETRYING ...]");
                numTimeouts++;
            }
        }

        if (response.length == 0)
            System.out.println("[ERROR][SERVER UNCONTACTABLE]");

        // Bump up message ID
        this.messageCounter += 1;
        System.out.println("[DEBUG][BUMPED UP MESSAGE COUNTER TO " + this.messageCounter + "]");

        return response;

    }

    public static void main(String[] args) throws IOException {
       
       


        int userID = 0;
        int facility = 0; // Facility type
        int facilityNumber = 0; // Facility Selection
        int start = 0; // Booking start time
        int stop = 0; // Booking stop time
        int dayOfWeek = 0;
        int bookingId = 0;
        String date;
        String receivedString;
        byte[] facilityType;
        byte[] monitorDuration;
        byte[] facilitySelection;
        byte[] dayOfBooking;
        byte[] startTime;
        byte[] endTime;
        byte[] daySelection;
        byte[] userId;
        byte[] bookingID;
        byte[] response;
        byte[] offsetValue;
        byte[] request;

        System.out.println("====================================");
        System.out.println("Welcome to Facility Booking System !");
        // Construct client and test sending dummies through to server
        String host = "10.27.249.49";
        // String host = "localhost";
        int port = 50001;
        int timeout = 1;
        boolean atMostOnce = false;
        boolean simulateFail = true;
        double probFailure = 0.2;

        // About payload to create a message
        byte communicationMethod;
        byte requestType;
        int messageID;
        byte[] payload;
        int payloadSize;

        // Max size or not
        boolean useMaxSize = false;

        // For response
        byte serverCommMethod;
        byte serverMsgType;
        int serverMsgID;
        byte[] serverPayload;
        int serverPayloadSize;

        // Client client = new Client(host,port);
        Client client = new Client(host, port, timeout, atMostOnce, simulateFail, probFailure);

        // Main console flow
        boolean quit = false;
        int choice;
        Scanner sc = new Scanner(System.in);

        while (!quit) {
            System.out.println("====================================");
            System.out.println("1. Check Facility Availibility.");
            System.out.println("2. Book Facility.");
            System.out.println("3. Change Booking Slot");
            System.out.println("4. Monitor Facility Availibility");
            System.out.println("5. Cancel Booking");
            System.out.println("6. Extend Booking Slot");
            System.out.println("7. Quit Program.");
            System.out.println("====================================");
            System.out.print("Please select an option: ");

            choice = sc.nextInt();

            System.out.println("====================================");

            switch (choice) {
            case 1:
                System.out.println("CHECK FACILITY AVAILIBILITY");
                System.out.println("Please enter facility type: ");
                System.out.println("====================================");
                System.out.println("1. Learning Pod");
                System.out.println("2. Lecture Theatre");
                System.out.println("3. Tutorial Room");
                System.out.println("4. Language Room");
                System.out.println("====================================");

                facility = sc.nextInt();
                switch (facility) {
                case 1:
                    System.out.println("Please select facility number: ");
                    System.out.println("====================================");
                    System.out.println("1. Learning Pod 1");
                    System.out.println("2. Learning Pod 2");
                    System.out.println("====================================");
                    break;
                case 2:
                    System.out.println("Please select facility number: ");
                    System.out.println("====================================");
                    System.out.println("1. Lecture Theatre 1");
                    System.out.println("2. Lecture Theatre 2");
                    System.out.println("====================================");
                    break;
                case 3:
                    System.out.println("Please select facility number: ");
                    System.out.println("====================================");
                    System.out.println("1. Tutorial Room 1");
                    System.out.println("2. Tutorial Room 2");
                    System.out.println("====================================");
                    break;
                case 4:
                    System.out.println("Please select facility number: ");
                    System.out.println("====================================");
                    System.out.println("1. Language Room 1");
                    System.out.println("2. Language Room 2");
                    System.out.println("====================================");
                    break;
                }
                facilityNumber = sc.nextInt();

                System.out.print("Please enter days [ e.g. 1-7 (range) or 1 (number) ]: ");
                // TODO Delimt by "-"
                date = sc.next();
                date = date.replace("-", "");
                System.out.println(date);

                // Marshal String and String to byte array form then stick them together
                // First 2B
                communicationMethod = 1;
                requestType = (byte) ((choice));
                // Message ID = current message sent number
                messageID = client.getCurMsgCount();
                // Payload - Marshal step
                facilityType = Util.marshall(facility);
                facilitySelection = Util.marshall(facilityNumber);
                dayOfBooking = Util.marshall(date);
                // Form payload
                payloadSize = facilityType.length + facilitySelection.length + dayOfBooking.length;
                payload = new byte[payloadSize];
                System.arraycopy(facilityType, 0, payload, 0, facilityType.length);
                System.arraycopy(facilitySelection, 0, payload, facilityType.length, facilitySelection.length);
                System.arraycopy(dayOfBooking, 0, payload, facilityType.length + facilitySelection.length,
                        dayOfBooking.length);

                // DEBUG
                System.out.println("[DEBUG][SENT TO SERVER - METHOD: " + communicationMethod + ", MESS_TYPE: "
                        + requestType + ", MESS_ID: " + messageID + ", SIZE: " + payloadSize + ", DATA: "
                        + Util.encodeHexString(payload) + "]");

                // Create Message
                request = Util.getMessageByte(communicationMethod, requestType, messageID, payloadSize, payload);

                // Send and Receive
                useMaxSize = true;
                response = client.routineSendReceive(request, useMaxSize);

                // server sent data
                serverCommMethod = Util.getCommMethod(response);
                serverMsgType = Util.getMsgType(response);
                serverMsgID = Util.getMsgID(response);
                serverPayloadSize = Util.getPayloadSize(response);
                serverPayload = Util.getPayload(response);

                // Display for debug purpose
                System.out.println("[DEBUG][SENT FROM SERVER - METHOD: " + serverCommMethod + ", MESS_TYPE: "
                        + serverMsgType + ", MESS_ID: " + serverMsgID + ", SIZE: " + serverPayloadSize + ", DATA: "
                        + Util.encodeHexString(serverPayload) + "]");

                // Demarshall and shown
                receivedString = Util.unmarshallString(serverPayload);
                System.out.println("Response: \n" + receivedString);

                break;

            case 2:
                // Assumption can only book 1 day and max 2 slots ?
                // Do we allow bulk booking ?
                System.out.println("BOOK FACILITY");
                System.out.println("Please enter facility type: ");
                System.out.println("====================================");
                System.out.println("1. Learning Pod");
                System.out.println("2. Lecture Theatre");
                System.out.println("3. Tutorial Room");
                System.out.println("4. Language Room");
                System.out.println("====================================");
                facility = sc.nextInt();

                switch (facility) {
                case 1:
                    System.out.println("Please select facility number: ");
                    System.out.println("====================================");
                    System.out.println("1. Learning Pod 1");
                    System.out.println("2. Learning Pod 2");
                    System.out.println("====================================");
                    break;
                case 2:
                    System.out.println("Please select facility number: ");
                    System.out.println("====================================");
                    System.out.println("1. Lecture Theatre 1");
                    System.out.println("2. Lecture Theatre 2");
                    System.out.println("====================================");
                    break;
                case 3:
                    System.out.println("Please select facility number: ");
                    System.out.println("====================================");
                    System.out.println("1. Tutorial Room 1");
                    System.out.println("2. Tutorial Room 2");
                    System.out.println("====================================");
                    break;
                case 4:
                    System.out.println("Please select facility number: ");
                    System.out.println("====================================");
                    System.out.println("1. Language Room 1");
                    System.out.println("2. Language Room 2");
                    System.out.println("====================================");
                    break;
                }
                facilityNumber = sc.nextInt();
                System.out.print("Please enter day: ");
                System.out.println("====================================");
                System.out.println("1. Monday");
                System.out.println("2. Tuesday");
                System.out.println("3. Wednesday");
                System.out.println("4. Thursday");
                System.out.println("5. Friday");
                System.out.println("6. Saturday");
                System.out.println("7. Sunday");
                System.out.println("====================================");
                dayOfWeek = sc.nextInt();
                System.out.print("Please choose start time: ");
                System.out.println("====================================");
                System.out.println("1. 8:00 am");
                System.out.println("2. 8:30 am");
                System.out.println("3. 9:00 am");
                System.out.println("4. 9:30 am");
                System.out.println("5. 10:00 am");
                System.out.println("6. 10:30 am");
                System.out.println("7. 11:00 am");
                System.out.println("8. 11:30 am");
                System.out.println("9. 12:00 pm");
                System.out.println("10. 12:30 pm");
                System.out.println("11. 1:00 pm");
                System.out.println("12. 1:30 pm");
                System.out.println("13. 2:00 pm");
                System.out.println("14. 2:30 pm");
                System.out.println("15. 3:00 pm");
                System.out.println("16. 3:30 pm");
                System.out.println("17. 4:00 pm");
                System.out.println("18. 4:30 pm");
                System.out.println("====================================");
                start = sc.nextInt();

                System.out.print("Please enter end time: ");
                System.out.println("====================================");
                System.out.println("1. 8:30 am");
                System.out.println("2. 9:00 am");
                System.out.println("3. 9:30 am");
                System.out.println("4. 10:00 am");
                System.out.println("5. 10:30 am");
                System.out.println("6. 11:00 am");
                System.out.println("7. 11:30 am");
                System.out.println("8. 12:00 pm");
                System.out.println("9. 12:30 pm");
                System.out.println("10. 1:00 pm");
                System.out.println("11. 1:30 pm");
                System.out.println("12. 2:00 pm");
                System.out.println("13. 2:30 pm");
                System.out.println("14. 3:00 pm");
                System.out.println("15. 3:30 pm");
                System.out.println("16. 4:00 pm");
                System.out.println("17. 4:30 pm");
                System.out.println("18. 5:00 pm");
                System.out.println("====================================");
                stop = sc.nextInt();

                // First 2B
                communicationMethod = 1;
                requestType = (byte) ((choice));
                // Message ID = current message sent number
                messageID = client.getCurMsgCount();
                // Payload - Marshal step
                facilityType = Util.marshall(facility);
                facilitySelection = Util.marshall(facilityNumber);
                daySelection = Util.marshall(dayOfWeek);
                startTime = Util.marshall(start);
                endTime = Util.marshall(stop);
                userId = Util.marshall(userID);
                // Form payload
                payloadSize = facilityType.length + facilitySelection.length + daySelection.length + startTime.length
                        + endTime.length + userId.length;
                payload = new byte[payloadSize];
                System.arraycopy(facilityType, 0, payload, 0, facilityType.length);
                System.arraycopy(facilitySelection, 0, payload, facilityType.length, facilitySelection.length);
                System.arraycopy(daySelection, 0, payload, facilityType.length + facilitySelection.length,
                        daySelection.length);
                System.arraycopy(startTime, 0, payload,
                        facilityType.length + facilitySelection.length + daySelection.length, startTime.length);
                System.arraycopy(endTime, 0, payload,
                        facilityType.length + facilitySelection.length + daySelection.length + startTime.length,
                        endTime.length);
                System.arraycopy(userId, 0, payload, facilityType.length + facilitySelection.length
                        + daySelection.length + startTime.length + endTime.length, userId.length);

                // DEBUG
                System.out.println("[DEBUG][SENT TO SERVER - METHOD: " + communicationMethod + ", MESS_TYPE: "
                        + requestType + ", MESS_ID: " + messageID + ", SIZE: " + payloadSize + ", DATA: "
                        + Util.encodeHexString(payload) + "]");

                // Create Message
                request = Util.getMessageByte(communicationMethod, requestType, messageID, payloadSize, payload);

                // Send and Receive
                response = client.routineSendReceive(request, useMaxSize);

                // server sent data
                serverCommMethod = Util.getCommMethod(response);
                serverMsgType = Util.getMsgType(response);
                serverMsgID = Util.getMsgID(response);
                serverPayloadSize = Util.getPayloadSize(response);
                serverPayload = Util.getPayload(response);

                // Display for debug purpose
                System.out.println("[DEBUG][SENT FROM SERVER - METHOD: " + serverCommMethod + ", MESS_TYPE: "
                        + serverMsgType + ", MESS_ID: " + serverMsgID + ", SIZE: " + serverPayloadSize + ", DATA: "
                        + Util.encodeHexString(serverPayload) + "]");

                receivedString = Util.unmarshallString(serverPayload);
                System.out.println("Response: \n" + receivedString);

                break;

            case 3:
                System.out.println("CHANGE BOOKING SLOT");
                System.out.print("Please enter booking confirmation ID: ");
                bookingId = sc.nextInt();
                System.out.print("Please enter offset (+ delay - forward): ");
                int offset = sc.nextInt();

                // First 2B
                communicationMethod = 1;
                requestType = (byte) ((choice));
                // Message ID = current message sent number
                messageID = client.getCurMsgCount();
                // Payload - Marshal step
                bookingID = Util.marshall(bookingId);
                offsetValue = Util.marshall(offset);
                // Form payload
                payloadSize = bookingID.length + offsetValue.length;
                payload = new byte[payloadSize];
                System.arraycopy(bookingID, 0, payload, 0, bookingID.length);
                System.arraycopy(offsetValue, 0, payload, bookingID.length, offsetValue.length);

                // DEBUG
                System.out.println("[DEBUG][SENT TO SERVER - METHOD: " + communicationMethod + ", MESS_TYPE: "
                        + requestType + ", MESS_ID: " + messageID + ", SIZE: " + payloadSize + ", DATA: "
                        + Util.encodeHexString(payload) + "]");

                // Create Message
                request = Util.getMessageByte(communicationMethod, requestType, messageID, payloadSize, payload);

                response = client.routineSendReceive(request, useMaxSize);

                // server sent data
                serverCommMethod = Util.getCommMethod(response);
                serverMsgType = Util.getMsgType(response);
                serverMsgID = Util.getMsgID(response);
                serverPayloadSize = Util.getPayloadSize(response);
                serverPayload = Util.getPayload(response);

                // Display for debug purpose
                System.out.println("[DEBUG][SENT FROM SERVER - METHOD: " + serverCommMethod + ", MESS_TYPE: "
                        + serverMsgType + ", MESS_ID: " + serverMsgID + ", SIZE: " + serverPayloadSize + ", DATA: "
                        + Util.encodeHexString(serverPayload) + "]");

                receivedString = Util.unmarshallString(serverPayload);
                System.out.println("Response: \n" + receivedString);

                break;

            case 4:
                System.out.println("MONITOR FACILITY AVAILIBILITY");
                System.out.println("Please enter facility type: ");
                System.out.println("====================================");
                System.out.println("1. Learning Pod");
                System.out.println("2. Lecture Theatre");
                System.out.println("3. Tutorial Room");
                System.out.println("4. Language Room");
                System.out.println("====================================");
                facility = sc.nextInt();

                switch (facility) {
                case 1:
                    System.out.println("Please select facility number: ");
                    System.out.println("====================================");
                    System.out.println("1. Learning Pod 1");
                    System.out.println("2. Learning Pod 2");
                    System.out.println("====================================");
                    break;
                case 2:
                    System.out.println("Please select facility number: ");
                    System.out.println("====================================");
                    System.out.println("1. Lecture Theatre 1");
                    System.out.println("2. Lecture Theatre 2");
                    System.out.println("====================================");
                    break;
                case 3:
                    System.out.println("Please select facility number: ");
                    System.out.println("====================================");
                    System.out.println("1. Tutorial Room 1");
                    System.out.println("2. Tutorial Room 2");
                    System.out.println("====================================");
                    break;
                case 4:
                    System.out.println("Please select facility number: ");
                    System.out.println("====================================");
                    System.out.println("1. Language Room 1");
                    System.out.println("2. Language Room 2");
                    System.out.println("====================================");
                    break;
                }
                facilityNumber = sc.nextInt();

                System.out.print("Please enter monitor duration (secs): ");
                int duration = sc.nextInt();

                // First 2B
                communicationMethod = 1;
                requestType = (byte) ((choice));
                // Message ID = current message sent number
                messageID = client.getCurMsgCount();
                // Payload - Marshal step
                facilityType = Util.marshall(facility);
                facilitySelection = Util.marshall(facilityNumber);
                monitorDuration = Util.marshall(duration);

                // Form payload
                payloadSize = facilityType.length + facilitySelection.length + monitorDuration.length;
                payload = new byte[payloadSize];
                System.arraycopy(facilityType, 0, payload, 0, facilityType.length);
                System.arraycopy(facilitySelection, 0, payload, facilityType.length, facilitySelection.length);
                System.arraycopy(monitorDuration, 0, payload, facilitySelection.length + facilityType.length,
                        monitorDuration.length);

                // DEBUG
                System.out.println("[DEBUG][SENT TO SERVER - METHOD: " + communicationMethod + ", MESS_TYPE: "
                        + requestType + ", MESS_ID: " + messageID + ", SIZE: " + payloadSize + ", DATA: "
                        + Util.encodeHexString(payload) + "]");

                // Create Message
                request = Util.getMessageByte(communicationMethod, requestType, messageID, payloadSize, payload);

                // Send and Receive
                response = client.routineSendReceive(request, useMaxSize);

                // server sent data
                serverCommMethod = Util.getCommMethod(response);
                serverMsgType = Util.getMsgType(response);
                serverMsgID = Util.getMsgID(response);
                serverPayloadSize = Util.getPayloadSize(response);
                serverPayload = Util.getPayload(response);

                // Display for debug purpose
                System.out.println("[DEBUG][SENT FROM SERVER - METHOD: " + serverCommMethod + ", MESS_TYPE: "
                        + serverMsgType + ", MESS_ID: " + serverMsgID + ", SIZE: " + serverPayloadSize + ", DATA: "
                        + Util.encodeHexString(serverPayload) + "]");

                receivedString = Util.unmarshallString(serverPayload);
                System.out.println(receivedString);
                Long t3 = Long.parseLong(receivedString);
                Long timeToEnd = System.currentTimeMillis() - (t3 + (Long.valueOf(duration) * 1000));
                //Convert t3(Long) to date
                Date datee = new Date(t3);
                DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                format.setTimeZone(TimeZone.getTimeZone("Singapore"));
                String formatted = format.format(datee);
                formatted = format.format(datee);
                System.out.println("[INFO][START OF CALLBACK IS " + formatted + "]");
                
                while (timeToEnd < 0) {
                    System.out.println("[DEBUG][TIME TO END IS " + timeToEnd.toString() + "]");
                    try {
                        response = client.receiveCallback(duration, t3);
                        // server sent data
                        serverCommMethod = Util.getCommMethod(response);
                        serverMsgType = Util.getMsgType(response);
                        serverMsgID = Util.getMsgID(response);
                        serverPayloadSize = Util.getPayloadSize(response);
                        serverPayload = Util.getPayload(response);

                        // Display for debug purpose
                        System.out.println("[DEBUG][CALLBACK FROM SERVER - METHOD: " + serverCommMethod
                                + ", MESS_TYPE: " + serverMsgType + ", MESS_ID: " + serverMsgID + ", SIZE: "
                                + serverPayloadSize + ", DATA: " + Util.encodeHexString(serverPayload) + "]");
                        System.out.println();

                        receivedString = Util.unmarshallString(serverPayload);
                        System.out.println("Response: \n" + receivedString);
                    } catch (Exception e) {
                        // TODO: handle exception
                        System.out.println("[DEBUG][CALLBACK TIME'S OUT]");
                    } finally {
                        // Update the time left for callback
                        timeToEnd = System.currentTimeMillis() - (t3 + (Long.valueOf(duration) * 1000));
                    }
                }

                Date date1 = new Date(System.currentTimeMillis());
                formatted = format.format(date1);
                System.out.println("[INFO][END OF CALLBACK IS " + formatted + "]");
                break;

            case 5:
                System.out.println("CANCEL BOOKING");
                System.out.print("Please enter booking ID: ");
                bookingId = sc.nextInt();

                // First 2B
                communicationMethod = 1;
                requestType = (byte) ((choice));
                // Message ID = current message sent number
                messageID = client.getCurMsgCount();
                // Payload - Marshal step
                bookingID = Util.marshall(bookingId);
                // Form payload
                payloadSize = bookingID.length;
                payload = bookingID;

                // DEBUG
                System.out.println("[DEBUG][SENT TO SERVER - METHOD: " + communicationMethod + ", MESS_TYPE: "
                        + requestType + ", MESS_ID: " + messageID + ", SIZE: " + payloadSize + ", DATA: "
                        + Util.encodeHexString(payload) + "]");

                // Create message
                request = Util.getMessageByte(communicationMethod, requestType, messageID, payloadSize, payload);

                response = client.routineSendReceive(request, useMaxSize);

                // server sent data
                serverCommMethod = Util.getCommMethod(response);
                serverMsgType = Util.getMsgType(response);
                serverMsgID = Util.getMsgID(response);
                serverPayloadSize = Util.getPayloadSize(response);
                serverPayload = Util.getPayload(response);

                // Display for debug purpose
                System.out.println("[DEBUG][SENT FROM SERVER - METHOD: " + serverCommMethod + ", MESS_TYPE: "
                        + serverMsgType + ", MESS_ID: " + serverMsgID + ", SIZE: " + serverPayloadSize + ", DATA: "
                        + Util.encodeHexString(serverPayload) + "]");

                receivedString = Util.unmarshallString(serverPayload);
                System.out.println("Response: \n" + receivedString);

                break;

            case 6:
                System.out.println("EXTEND BOOKING SLOT");
                System.out.print("Please enter booking confirmation ID: ");
                bookingId = sc.nextInt();
                System.out.print("Please enter offset (+ extend - shorten): ");
                offset = sc.nextInt();

                // First 2B
                communicationMethod = 1;
                requestType = (byte) ((choice));
                // Message ID = current message sent number
                messageID = client.getCurMsgCount();
                // Payload - Marshal step
                bookingID = Util.marshall(bookingId);
                offsetValue = Util.marshall(offset);
                // Form payload
                payloadSize = bookingID.length + offsetValue.length;
                payload = new byte[payloadSize];
                System.arraycopy(bookingID, 0, payload, 0, bookingID.length);
                System.arraycopy(offsetValue, 0, payload, bookingID.length, offsetValue.length);

                // DEBUG
                System.out.println("[DEBUG][SENT TO SERVER - METHOD: " + communicationMethod + ", MESS_TYPE: "
                        + requestType + ", MESS_ID: " + messageID + ", SIZE: " + payloadSize + ", DATA: "
                        + Util.encodeHexString(payload) + "]");

                // Create Message
                request = Util.getMessageByte(communicationMethod, requestType, messageID, payloadSize, payload);

                response = client.routineSendReceive(request, useMaxSize);

                // server sent data
                serverCommMethod = Util.getCommMethod(response);
                serverMsgType = Util.getMsgType(response);
                serverMsgID = Util.getMsgID(response);
                serverPayloadSize = Util.getPayloadSize(response);
                serverPayload = Util.getPayload(response);

                // Display for debug purpose
                System.out.println("[DEBUG][SENT FROM SERVER - METHOD: " + serverCommMethod + ", MESS_TYPE: "
                        + serverMsgType + ", MESS_ID: " + serverMsgID + ", SIZE: " + serverPayloadSize + ", DATA: "
                        + Util.encodeHexString(serverPayload) + "]");
                receivedString = Util.unmarshallString(serverPayload);
                System.out.println("Response: \n" + receivedString);

                break;

            case 7:
                System.out.println("SHUTTING DOWN");
                quit = true;
                sc.close();
                // Close the client socket
                client.clientSocket.close();
                System.out.println("... DONE");
                break;

            default:
                System.out.println("Invald option! Please try again !");
            }
            System.out.println();

        }

    }

}