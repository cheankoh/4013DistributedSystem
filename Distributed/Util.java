package Distributed;

public class Util {
    // Implements marshalling and demarshalling logic
    // To run anywhere, should be of public static type

    //Marshalling Int
    public static byte[] marshall(int x){
        byte[] arrayByte = new byte[1];
        return arrayByte;
    }

    //Demarshalling Int
    public static int unmarshallInt(byte[] x){
        return -1;
    }

    //Marshalling String
    public static byte[] marshall(String s){
        byte[] arrayByte = new byte[1];
        return arrayByte;
    }

    //Demarshalling String
    public static String demarshall(byte[] s){
        String aString = "";
        return aString;
    } 
}
