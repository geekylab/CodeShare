package net.geekylab.codeshare.network.tcp;

import net.geekylab.codeshare.network.TransportListener;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by johna on 13/09/14.
 * by Geekylab
 */
public class TCPServerTransport {

    private static final int BUFSIZE = 5000;
    private final ServerSocket ssock;
    private TransportListener transportListener;

    public TCPServerTransport(String host, int port, TransportListener transportListener) throws IOException {
        this.transportListener = transportListener;
        this.ssock = new ServerSocket();
        this.ssock.bind(new InetSocketAddress(host, port));
    }

    public void accept() throws IOException {

        int recvMsgSize; // 受信メッセージサイズ
        byte[] receiveBuf = new byte[BUFSIZE]; // 受信バッファ

        while (true) {
            Socket socket = null;

            System.out.println("waiting for connection....");
            TCPTransport tcp = null;

            try {
                socket = this.ssock.accept();
                tcp = new TCPTransport(socket, this.transportListener);
                this.transportListener.onConnected(tcp);
                tcp.doProcess();
            } finally {
                if (tcp != null) {
                    tcp.close();
                }
            }
        }
    }

    public boolean isRunning() {
        return !this.ssock.isClosed();
    }

    public void close() throws IOException {
        System.out.println("Close");
        this.ssock.close();
    }
}
