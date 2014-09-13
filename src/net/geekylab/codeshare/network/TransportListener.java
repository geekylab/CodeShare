package net.geekylab.codeshare.network;

/**
 * Created by johna on 13/09/14.
 */
public interface TransportListener {
    public void onReceivedFromTransport(Transport transport, String message);

    public void onConnected(Transport transport);

    public void onDisconnected(Transport transport);
}
