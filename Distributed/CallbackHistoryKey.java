package Distributed;

import java.net.InetAddress;
import java.util.Objects;

public class CallbackHistoryKey {
    private final InetAddress IPAddress;
    private final int port;

    public CallbackHistoryKey(InetAddress IPAddress, int port) {
        this.IPAddress = IPAddress;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof HistoryKey))
            return false;
        CallbackHistoryKey key = (CallbackHistoryKey) o;
        return port == key.port && IPAddress.equals(key.IPAddress);
    }

    @Override
    public int hashCode() {

        return Objects.hash(IPAddress, port);
    }
}
