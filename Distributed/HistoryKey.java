package Distributed;
import java.util.Objects;

public class HistoryKey {
    private final String IPAddress;
    private final int port;
    
    public HistoryKey(String IPAddress, int port){
        this.IPAddress = IPAddress;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof HistoryKey))
            return false;
        HistoryKey key = (HistoryKey) o;
        return port == key.port && IPAddress.equals(key.IPAddress);
    }

    @Override
    public int hashCode() {

        return Objects.hash(IPAddress, port);
    }
}
