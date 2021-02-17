package Controller;

import Model.Booking;
import Model.Facility;
import Model.User;
import Controller.FileIO;

import java.util.ArrayList;
import java.io.IOException;


class DataFactory{


    public static void main(String[] args) {
    User user1 = new User();
    User user2 = new User();

    user1.setUserID(1234);
    user2.setUserID(4567);

    ArrayList<User> Userlist= new ArrayList<User>();

    Userlist.add(user1);
    Userlist.add(user2);

    ////Write userlist object into testuser.txt
    FileIO.storeUserData(user1);
    User testUser = FileIO.getUserData();
    try{
        FileIO.writeObject("testusers.txt", Userlist);
    }catch (IOException e){
        System.out.println("File not found. please try again.");
    }


    //Read testuser.txt and display user stored
    // ArrayList<User> Userlistreceive= new ArrayList<User>();

    // try{
    //     Userlistreceive = (ArrayList<User>) FileIO.readObject("testusers.txt");
    // }
    // catch (ClassNotFoundException | IOException e) {
    //     System.out.println("File is missing. Please try again");
    // }

    // System.out.println(Userlistreceive);
   



    }

}
