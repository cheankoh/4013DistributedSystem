package Distributed;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

//JUST TO TEST
import java.nio.charset.StandardCharsets;

public class Client{
    /* Attributes for client */
    private DatagramSocket clientSocket;
    private InetAddress hostIP;
    private int hostPort;
    //TODO: ADD IN OTHER STUFFS LIKE TIMEOUT, INVOCATION SEMANTICS, etc

    public Client(String hostIP, int hostPort) throws SocketException, UnknownHostException
    {
        this.clientSocket = new DatagramSocket();
        this.hostIP = InetAddress.getByName(hostIP);
        this.hostPort = hostPort;
    }

    //TODO: Add setters for other attributes when added.

    //Sending a MARSHALLED byte array over the server
    public void send(byte[] message) throws IOException
    {
        //Create a datagram packet before sending
        DatagramPacket sendingPacket = new DatagramPacket(message, message.length, this.hostIP, this.hostPort);
        this.clientSocket.send(sendingPacket);
    }

    //Receiving a MARSHALLED byte array over the UDP network
    public byte[] receive() throws IOException
    {
        // TODO: How to determine what is the length of the receiving packet
        // TODO: So that we can create the buffere accordingly
        
        // Hard code first. We may assume that only server send message to a client
        byte[] messageBuffer = new byte[1024];
        DatagramPacket receivingPacket = new DatagramPacket(messageBuffer, messageBuffer.length);
        
        //Receive
        clientSocket.receive(receivingPacket);
        
        return messageBuffer; 
    }

    //Main thread of the client
    public static void main(String[] args) throws IOException{
        System.out.println("====================================");
        System.out.println("Welcome to Facility Booking System !");
        // Construct client and test sending dummies through to server
        String host = "localhost";
        int port = 50001;
        Client client = new Client(host,port);

        //TODO: Do a console IO stuffs here
        boolean quit = false;
        int choice;
        Scanner sc = new Scanner(System.in);

        while (!quit){
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

            switch(choice){
                case 1:
                    System.out.println("CHECK FACILITY AVAILIBILITY");
                    System.out.println("Please enter facility type: ");
                    System.out.println("====================================");
                    System.out.println("1. Learning Pod");
                    System.out.println("2. Lecture Theatre");
                    System.out.println("3. Tutorial Room");
                    System.out.println("4. Language Room");
                    System.out.println("====================================");

                    int facility = sc.nextInt();

                    switch(facility){
                        case 1:     System.out.println("Please select facility number: ");
                                    System.out.println("====================================");
                                    System.out.println("1. Learning Pod 1");
                                    System.out.println("2. Learning Pod 2");
                                    System.out.println("====================================");
                                    break;
                        case 2:     System.out.println("Please select facility number: ");
                                    System.out.println("====================================");
                                    System.out.println("1. Lecture Theatre 1");
                                    System.out.println("2. Lecture Theatre 2");
                                    System.out.println("====================================");
                                    break;                    
                        case 3:     System.out.println("Please select facility number: ");
                                    System.out.println("====================================");
                                    System.out.println("1. Tutorial Room 1");
                                    System.out.println("2. Tutorial Room 2");
                                    System.out.println("====================================");
                                    break;                    
                        case 4:     System.out.println("Please select facility number: ");
                                    System.out.println("====================================");
                                    System.out.println("1. Language Room 1");
                                    System.out.println("2. Language Room 2");
                                    System.out.println("====================================");
                                    break;                    }
                    int facilityNumber = sc.nextInt();

                    System.out.print("Please enter days [ e.g. 1-7 (range) or 1 (number) ]: ");
                    //TODO Delimt by "-"
                    String date = sc.next();
                    date = date.replace("-","");
                    //Marshal String and String to byte array form then stick them together

                    byte[] requestID = Integer.toString(choice).getBytes();
                    byte[] facilityType = Integer.toString(facility).getBytes();
                    byte[] facilitySelection = Integer.toString(facilityNumber).getBytes();
                    byte[] dayOfBooking = date.getBytes();
                    byte[] request = new byte[requestID.length + facilityType.length+ facilitySelection.length+dayOfBooking.length];
                    System.arraycopy(requestID, 0, request, 0, requestID.length);
                    System.arraycopy(facilityType, 0, request, requestID.length, facilityType.length);
                    System.arraycopy(facilitySelection, 0, request, requestID.length+facilityType.length, facilitySelection.length);
                    System.arraycopy(dayOfBooking, 0, request, requestID.length+facilityType.length+facilitySelection.length, dayOfBooking.length);

                    //Send
                    client.send(request);

                    //Receive
                    byte[] response = client.receive();

                    //Demarshall
                    String receivedString = new String(response, StandardCharsets.UTF_8);
                    System.out.println("Response: \n" + receivedString);

                    break;

                case 2:
                    // Assumption can only book 1 day and max 2 slots ?
                    // Do we allow bulk booking ?
                    System.out.println("BOOK FACILITY");
                    System.out.print("Please enter facility: ");
                    facility = sc.nextInt();
                    System.out.print("Please enter day: ");
                    date = sc.next();
                    System.out.print("Please enter start time: ");
                    float start = sc.nextFloat();
                    System.out.print("Please enter end time: ");
                    float stop = sc.nextFloat();

                    // Next steps same as above
                    // TODO Marshal float -> byte
                    break;

                case 3:
                    System.out.println("CHANGE BOOKING SLOT");
                    System.out.print("Please enter booking confirmation ID: ");
                    String bookingId = sc.next();
                    System.out.print("Please enter offset (+ delay - forward): ");
                    int offset = sc.nextInt();

                    // Next steps same as above
                    // TODO Marshal int -> byte

                    break;

                case 4:
                    System.out.println("MONITOR FACILITY AVAILIBILITY");
                    System.out.print("Please enter facility: ");
                    facility = sc.nextInt();
                    System.out.print("Please enter duration (secs): ");
                    int duration = sc.nextInt();

                    //TODO Some While loop to block

                    break;

                case 5:
                    System.out.println("CANCEL BOOKING");
                    System.out.print("Please enter booking confirmation ID: ");
                    bookingId = sc.next();
                    break;

                case 6:
                    System.out.println("EXTEND BOOKING SLOT");
                    System.out.print("Please enter booking confirmation ID: ");
                    bookingId = sc.next();
                    System.out.print("Please enter offset (+ extend - shorten): ");
                    offset = sc.nextInt();

                    break;

                case 7:
                    System.out.println("SHUTTING DOWN");
                    quit = true;
                    //Close the client socket
                    client.clientSocket.close();
                    System.out.println("... DONE");
                    break;

                default:
                    System.out.println("Invald option! Please try again !");
            }

        }




    }

}

