package net.geekylab.codeshare.network;

import java.io.*;
import java.net.*;

public class PeerServer implements Runnable {

    public static final int PORT = 12033;
    protected DatagramSocket socket = null;
    protected boolean running = false;

    public static void main(String[] args) throws IOException, InterruptedException {

        PeerServer discoveryServerThread = new PeerServer();
        Thread thread1 = new Thread(discoveryServerThread);

        thread1.start();
        thread1.join();

    }

    public static PeerServer getInstance() {
        return DiscoveryServerThreadHolder.INSTANCE;
    }

    public void stopServer() {
        if (!socket.isClosed()) {
            socket.close();
        }
    }

    private static class DiscoveryServerThreadHolder {
        private static final PeerServer INSTANCE = new PeerServer();
    }

    public boolean isRunning() {
        return running;
    }

    synchronized public boolean getRunning() {
        return running;
    }

    public void run() {
        try {
            running = true;
            socket = new DatagramSocket(PORT, InetAddress.getByName("0.0.0.0"));
            while (getRunning()) {
                try {
                    System.out.println(getClass().getName() + ">>>Ready to receive broadcast packets!");
                    //Receive a packet
                    byte[] buf = new byte[15000];

                    // receive request
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);

                    //Packet received
                    System.out.println(getClass().getName() + ">>>Discovery packet received from: " + packet.getAddress().getHostAddress());
                    System.out.println(getClass().getName() + ">>>Packet received; data: " + new String(packet.getData()));

                    String message = new String(packet.getData()).trim();

                    if (message.equals("DISCOVER_SHARETAB_REQUEST")) {
                        byte[] sendData = "DISCOVER_SHARETAB_RESPONSE".getBytes();
                        //Send a response
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                        socket.send(sendPacket);

                        System.out.println(getClass().getName() + ">>>Sent packet to: " + sendPacket.getAddress().getHostAddress());
                    }
                } catch (IOException e) {
                    running = false;
                }
            }
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            running = false;
           e.printStackTrace();
        }
    }
}