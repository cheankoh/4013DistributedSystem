package Distributed;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

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
        System.out.println("Welcome to Facility Booking System! Intializing Client...");
        
        // Construct client and test sending dummies through to server
        String host = "localhost";
        int port = 50001;
        Client client = new Client(host,port);

        //Test sending a random message by converting it to byte (marshalling)
        String randomMsg = "Hello World from client side!";
        System.out.println("Sending message : " + randomMsg);
        byte[] byteToSend = randomMsg.getBytes();
        client.send(byteToSend);

        //Test receiving a message from server
        byte[] byteReceived = client.receive();
        String randomReceivedMsg = new String(byteReceived, StandardCharsets.UTF_8);
        System.out.println("Received message : " + randomReceivedMsg);

        //Test sending an ACK from client to server

        //TODO: Do a console IO stuffs here
    }

}

