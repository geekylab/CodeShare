package net.geekylab.codeshare.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by johna on 13/09/14.
 */
public interface Transport {
    public void connect();

    public void close() throws IOException;

    public void sendMessage(String message) throws IOException;

    public String recvMessage() throws IOException;

    public InputStream getInputStream();

    public OutputStream getOutputStream();
}
