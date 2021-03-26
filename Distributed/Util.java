package Distributed;

public class Util {
    public static final int MAX_SIZE = 1024;
    public static final int MAX_PAYLOAD = 1014;
    public static final int NORMAL_SIZE = 64;
    public static final int NORMAL_PAYLOAD = 54;
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

    //Create our messageByte information
    public static byte[] getMessageByte(byte commMethod, byte msgType, int msgID, int payloadSize, byte[] payload){
        byte[] toSendByte = new byte[NORMAL_SIZE];
        if (msgType == 1 && commMethod == 2)
            toSendByte = new byte[MAX_SIZE];
        //concatenation
        toSendByte[0] = commMethod;
        toSendByte[1] = msgType;
        byte[] messageID = marshall(msgID);
        byte[] size = marshall(payloadSize);
        System.arraycopy(messageID,0,toSendByte,2,messageID.length);
        System.arraycopy(size,0,toSendByte,6,size.length);
        System.arraycopy(payload,0,toSendByte,10,payload.length);
        return toSendByte;
    }

    //Extract common fields
    public static byte getCommMethod(byte[] messageByte){
        return messageByte[0];
    }

    public static byte getMsgType(byte[] messageByte){
        return messageByte[1];
    }

    public static int getMsgID(byte[] messageByte){
        byte[] messageIDByte = new byte[4];
        System.arraycopy(messageByte,2,messageIDByte,0,messageIDByte.length);
        return unmarshallInt(messageIDByte);
    }

    public static int getPayloadSize(byte[] messageByte){
        byte[] payloadSize = new byte[4];
        System.arraycopy(messageByte,6,payloadSize,0,payloadSize.length);
        return unmarshallInt(payloadSize);
    }
    
    public static byte[] getPayload(byte[] messageByte){
        int payloadSize = getPayloadSize(messageByte);
        byte[] messagePayload = new byte[payloadSize];
        System.arraycopy(messageByte,10,messagePayload,0,payloadSize);
        return messagePayload;
    }
    
    //Extract application payload
    public static int getFacilityType(byte[] payload)
    {
        byte[] factType = new byte[4];
        System.arraycopy(payload, 0, factType, 0, factType.length);
        return unmarshallInt(factType);
    }

    public static int getFacilityNum(byte[] payload)
    {
        byte[] factID = new byte[4];
        System.arraycopy(payload, 4, factID, 0, factID.length);
        return unmarshallInt(factID);
    }

    //TODO
    public static String getDate(byte[] payload)
    {
        return unmarshallString(payload);
    }

    public static int getDayOfWeek(byte[] payload)
    {
        byte[] dayOfWeek = new byte[4];
        System.arraycopy(payload, 8, dayOfWeek, 0, dayOfWeek.length);
        return unmarshallInt(dayOfWeek);
    }
    public static int getDuration(byte[] payload)
    {
        byte[] dayOfWeek = new byte[4];
        System.arraycopy(payload, 8, dayOfWeek, 0, dayOfWeek.length);
        return unmarshallInt(dayOfWeek);
    }

    public static int getStartSlot(byte[] payload)
    {
        byte[] start = new byte[4];
        System.arraycopy(payload, 12, start, 0, start.length);
        return unmarshallInt(start);
    }

    public static int getStopSlot(byte[] payload)
    {
        byte[] stop = new byte[4];
        System.arraycopy(payload, 16, stop, 0, stop.length);
        return unmarshallInt(stop);
    }

    public static int getUserID(byte[] payload)
    {
        byte[] userID = new byte[4];
        System.arraycopy(payload, 20, userID, 0, userID.length);
        return unmarshallInt(userID);
    }

    public static int getBookingID(byte[] payload)
    {
        byte[] bookingID = new byte[4];
        System.arraycopy(payload, 0, bookingID, 0, bookingID.length);
        return unmarshallInt(bookingID);
    }

    public static int getOffset(byte[] payload)
    {
        byte[] offset = new byte[4];
        System.arraycopy(payload, 4, offset, 0, offset.length);
        return unmarshallInt(offset);
    }


    //convert 1B to 2 Hex number
    public static String toHex(byte x) {
        char[] hexadecimal = new char[2];
        hexadecimal[0] = Character.forDigit((x >> 4) & 0xF, 16);
        hexadecimal[1] = Character.forDigit((x & 0xF), 16);
        return new String(hexadecimal);
    }
    
    //Convert a byte array into a hex string
    public static String encodeHexString(byte[] byteArray) {
        StringBuffer hexadecimalString = new StringBuffer();
        //Convert each byte to 2 Hex digits
        for (int i = 0; i < byteArray.length; i++) {
            hexadecimalString.append(toHex(byteArray[i]));
            hexadecimalString.append(' ');
        }
        return hexadecimalString.toString();
    }

    //Test
    public static void main(String[] args)
    {
        String aString = "I am an Idiot Sandwich 123456";
        int anInt = 123456789;

        //Marshall
        byte[] stringByte = marshall(aString);
        byte[] intByte = marshall(anInt);

        System.out.println(encodeHexString(stringByte));
        System.out.println(encodeHexString(intByte));

        //Demarshall
        String aString_ = unmarshallString(stringByte);
        int anInt_ = unmarshallInt(intByte);

        //Print out
        System.out.println("Demarshalled String: " + aString_);
        System.out.println("Demarshalled Int: " + anInt_);

    }
}
