package Distributed;

import javax.lang.model.util.ElementScanner6;

public class Util {
    // Implements marshalling and demarshalling logic
    // To run anywhere, should be of public static type

    //Marshalling Int
    public static byte[] marshall(int x){
        //Bit-shifting into an array
        byte[] arrayByte = new byte[]{
            (byte)((x >> 24) & 0xff),
            (byte)((x >> 16) & 0xff),
            (byte)((x >> 8) & 0xff),
            (byte)((x >> 0) & 0xff)
        };
        return arrayByte;
    }

    //Demarshalling Int
    public static int unmarshallInt(byte[] x){
        //Ensure that the byte array given is not empty or more than 4B (INT size)
        if (x == null || x.length != 4)
            return 0x0;
        else
        {
            int intValue = (int)(
                (0xff & x[0]) << 24 |
                (0xff & x[1]) << 16 |
                (0xff & x[2]) << 8  |
                (0xff & x[3]) << 0
            );
            return intValue;
        }
    }

    //Marshalling String
    public static byte[] marshall(String s){
        //Convert to char array of size 2 each
        char[] arrayChars = s.toCharArray();
        byte[] arrayByte = new byte[arrayChars.length*2];
        for (char c_index = 0; c_index < arrayChars.length; c_index++){
            arrayByte[2*c_index] =      (byte)((arrayChars[c_index] >> 8) & 0xff);
            arrayByte[2*c_index + 1] =  (byte)((arrayChars[c_index] >> 0) & 0xff);
        }
        return arrayByte;
    }

    //Demarshalling String
    public static String unmarshallString(byte[] s){
        //Convert 2 byte into 1 char
        char[] arrayChars = new char[s.length/2];
        for (int c_index = 0; c_index < arrayChars.length; c_index++){
            arrayChars[c_index] = (char)(
                (0xff & s[2*c_index])       << 8 |
                (0xff & s[2*c_index + 1])   << 0
            );
        }
        String stringValue = new String(arrayChars);
        return stringValue;
    }

    //Marshall a boolean
    public static byte[] marshallBool(boolean x){
        byte[] arrayByte = new byte[]{x ? (byte)1 : (byte)0};
        return arrayByte;
    }

    //Demarshall a boolean
    public static boolean unmarshallBool(byte[] x){
        if ((int)(x[0]) == 1) return true;
        else return false;
    }

    //Marshall an object + Demarshall : TODO if needed.

    //Test
    public static void main(String[] args)
    {
        String aString = "I am an Idiot Sandwich 123456";
        int anInt = 36590;

        //Marshall
        byte[] stringByte = marshall(aString);
        byte[] intByte = marshall(anInt);

        //Demarshall
        String aString_ = unmarshallString(stringByte);
        int anInt_ = unmarshallInt(intByte);

        //Print out
        System.out.println("Demarshalled String: " + aString_);
        System.out.println("Demarshalled Int: " + anInt_);

    }
}
