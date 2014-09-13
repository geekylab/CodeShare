package net.geekylab.codeshare.network.tcp;

import net.geekylab.codeshare.network.Transport;
import net.geekylab.codeshare.network.TransportListener;

import java.io.*;
import java.net.Socket;

/**
 * Created by johna on 13/09/14.
 */

public class TCPTransport implements Transport {

    private static final int BUFSIZE = 5000;
    private final TransportListener receiveTransportListener;
    private Socket sock;
    private InputStream in;
    private OutputStream out;

    public TCPTransport(TransportListener transportListener) {
        this.receiveTransportListener = transportListener;
    }

    public TCPTransport(Socket sock, TransportListener transportListener) throws IOException {
        this.receiveTransportListener = transportListener;
        this.sock = sock;
        this.in = sock.getInputStream();
        this.out = sock.getOutputStream();
    }

    public void doProcess() throws IOException {
        String message;
        message = recvMessage();
        this.receiveTransportListener.onReceivedFromTransport(this, message);
    }

    @Override
    public void connect() {

    }

    @Override
    public void close() throws IOException {
        this.sock.close();
    }

    @Override
    public void sendMessage(String message) throws IOException {
        byte[] str = message.getBytes();
        this.out.write(str, 0, str.length);
    }

    @Override
    public String recvMessage() throws IOException {
        Reader reader = new InputStreamReader(this.in);
        char[] buffer = new char[8192];
        StringBuilder stringBuffer = new StringBuilder();
        while (true) {
            int rsz = reader.read(buffer, 0, buffer.length);
            if (rsz < 0)
                break;
            stringBuffer.append(buffer, 0, rsz);
        }
        return stringBuffer.toString();
    }

    @Override
    public InputStream getInputStream() {
        return this.in;
    }

    @Override
    public OutputStream getOutputStream() {
        return this.out;
    }
}
