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

//JUST TO TEST
import java.nio.charset.StandardCharsets;

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
    }

    // TODO: Add setters for other attributes when added.

    // Sending a MARSHALLED byte array over the server
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
    public byte[] receive() throws IOException, SocketTimeoutException {
        // TODO: How to determine what is the length of the receiving packet
        // Hard code first - Best practice is to keep to one UDP and assume max possible
        // size (waste still better)
        byte[] messageBuffer = new byte[1024];
        DatagramPacket receivingPacket = new DatagramPacket(messageBuffer, messageBuffer.length);

        // Timeout in milliseconds
        clientSocket.setSoTimeout(this.requestTimeout * 1000);

        // Receive
        clientSocket.receive(receivingPacket);
        System.out.println("[INFO][RECEIVED REPLY BY SERVER]");

        return messageBuffer;
    }

    // Wrap send and receive together for reusibility
    public byte[] routineSendReceive(byte[] message) throws IOException, SocketTimeoutException {
        int numTimeouts = 0;
        int maxTimeouts = 5; // @TODO: Move to constants

        // dummy byte array of length 0
        byte[] response = new byte[0];

        // Retry if timeout up until a max value
        while (numTimeouts < maxTimeouts) {
            try {
                send(message);
                response = receive();
                break;
            } catch (SocketTimeoutException e) {
                System.out.println("[ERROR][REQUEST TIMEOUT. RETRYING ...]");
                numTimeouts++;
            }
        }

        if (response.length == 0)
            System.out.println("[ERROR][SERVER UNCONTACTABLE]");

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
        byte[] requestID;
        byte[] facilityType;
        byte[] facilitySelection;
        byte[] dayOfBooking;
        byte[] request;
        byte[] startTime;
        byte[] endTime;
        byte[] daySelection;
        byte[] userId;
        byte[] bookingID;
        byte[] response;
        byte[] offsetValue;
        System.out.println("====================================");
        System.out.println("Welcome to Facility Booking System !");
        // Construct client and test sending dummies through to server
        String host = "localhost";
        int port = 50001;
        int timeout = 1;
        boolean atMostOnce = false;
        boolean simulateFail = true;
        double probFailure = 0.2;

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
                // Marshal String and String to byte array form then stick them together

                requestID = Integer.toString(choice).getBytes();
                facilityType = Integer.toString(facility).getBytes();
                facilitySelection = Integer.toString(facilityNumber).getBytes();
                dayOfBooking = date.getBytes();
                request = new byte[requestID.length + facilityType.length + facilitySelection.length
                        + dayOfBooking.length];
                System.arraycopy(requestID, 0, request, 0, requestID.length);
                System.arraycopy(facilityType, 0, request, requestID.length, facilityType.length);
                System.arraycopy(facilitySelection, 0, request, requestID.length + facilityType.length,
                        facilitySelection.length);
                System.arraycopy(dayOfBooking, 0, request,
                        requestID.length + facilityType.length + facilitySelection.length, dayOfBooking.length);

                // Send and Receive
                response = client.routineSendReceive(request);

                // Demarshall
                receivedString = new String(response, StandardCharsets.UTF_8);
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

                requestID = Integer.toString(choice).getBytes();
                facilityType = Integer.toString(facility).getBytes();
                facilitySelection = Integer.toString(facilityNumber).getBytes();
                daySelection = Integer.toString(dayOfWeek).getBytes();
                startTime = Integer.toString(start).getBytes();
                endTime = Integer.toString(stop).getBytes();
                userId = Integer.toString(userID).getBytes();
                request = new byte[requestID.length + facilityType.length + facilitySelection.length
                        + daySelection.length + startTime.length + endTime.length + userId.length];
                System.arraycopy(requestID, 0, request, 0, requestID.length);
                System.arraycopy(facilityType, 0, request, requestID.length, facilityType.length);
                System.arraycopy(facilitySelection, 0, request, requestID.length + facilityType.length,
                        facilitySelection.length);
                System.arraycopy(daySelection, 0, request,
                        requestID.length + facilityType.length + facilitySelection.length, daySelection.length);
                System.arraycopy(startTime, 0, request,
                        requestID.length + facilityType.length + facilitySelection.length + daySelection.length,
                        startTime.length);
                System.arraycopy(endTime, 0, request, requestID.length + facilityType.length + facilitySelection.length
                        + daySelection.length + startTime.length, endTime.length);
                System.arraycopy(userId, 0, request, requestID.length + facilityType.length + facilitySelection.length
                        + daySelection.length + startTime.length + endTime.length, userId.length);
                // client.send(request);
                // System.out.println("sent to server");

                // Send and Receive
                response = client.routineSendReceive(request);

                // Demarshall
                receivedString = new String(response, StandardCharsets.UTF_8);
                System.out.println("Response: \n" + receivedString);

                // Parameters: facility id, start time, stop time

                // Next steps same as above
                // TODO Marshal float -> byte
                break;

            case 3:
                System.out.println("CHANGE BOOKING SLOT");
                System.out.print("Please enter booking confirmation ID: ");
                bookingId = sc.nextInt();
                System.out.print("Please enter offset (+ delay - forward): ");
                int offset = sc.nextInt();

                requestID = Integer.toString(choice).getBytes();
                bookingID = Integer.toString(bookingId).getBytes();
                offsetValue = Integer.toString(offset).getBytes();
                request = new byte[requestID.length + bookingID.length + offsetValue.length];
                System.arraycopy(requestID, 0, request, 0, requestID.length);
                System.arraycopy(bookingID, 0, request, requestID.length, bookingID.length);
                System.arraycopy(offsetValue, 0, request, requestID.length + bookingID.length, offsetValue.length);

                response = client.routineSendReceive(request);

                // Demarshall
                receivedString = new String(response, StandardCharsets.UTF_8);
                System.out.println("Response: \n" + receivedString);

                // Next steps same as above
                // TODO Marshal int -> byte

                break;

            case 4:
                System.out.println("MONITOR FACILITY AVAILIBILITY");
                System.out.print("Please enter facility: ");
                facility = sc.nextInt();
                System.out.print("Please enter duration (secs): ");
                int duration = sc.nextInt();

                // TODO Some While loop to block

                break;

            case 5:
                System.out.println("CANCEL BOOKING");
                System.out.print("Please enter booking ID: ");
                bookingId = sc.nextInt();

                requestID = Integer.toString(choice).getBytes();
                bookingID = Integer.toString(bookingId).getBytes();
                request = new byte[requestID.length + bookingID.length];
                System.arraycopy(requestID, 0, request, 0, requestID.length);
                System.arraycopy(bookingID, 0, request, requestID.length, bookingID.length);

                response = client.routineSendReceive(request);

                // Demarshall
                receivedString = new String(response, StandardCharsets.UTF_8);
                System.out.println("Response: \n" + receivedString);

                break;

            case 6:
                System.out.println("EXTEND BOOKING SLOT");
                System.out.print("Please enter booking confirmation ID: ");
                bookingId = sc.nextInt();
                System.out.print("Please enter offset (+ extend - shorten): ");
                offset = sc.nextInt();

                requestID = Integer.toString(choice).getBytes();
                bookingID = Integer.toString(bookingId).getBytes();
                offsetValue = Integer.toString(offset).getBytes();
                request = new byte[requestID.length + bookingID.length + offsetValue.length];
                System.arraycopy(requestID, 0, request, 0, requestID.length);
                System.arraycopy(bookingID, 0, request, requestID.length, bookingID.length);
                System.arraycopy(offsetValue, 0, request, requestID.length + bookingID.length, offsetValue.length);

                response = client.routineSendReceive(request);

                // Demarshall
                receivedString = new String(response, StandardCharsets.UTF_8);
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

        }

    }

}
