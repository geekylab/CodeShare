package net.geekylab.codeshare.network;

import com.google.common.io.CharStreams;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by johna on 09/09/14.
 *
 */
public class PeerDiscovery {
    private static final byte QUERY_PACKET = 80;
    private static final byte RESPONSE_PACKET = 81;
    private static final int PACKET_SIZE = 102400;

    public static final int PORT = 12033;
    private static PeerDiscovery INSTANCE;

    /**
     * The group identifier. Determines the set of peers that are able
     * to discover each other
     */
    public final int group;

    /**
     * The port number that we operate on
     */
    public final int port;
    private PeerDiscoverResponseInterface callback;

    /**
     * Data returned with discovery
     */
    public int peerData;

    private DatagramSocket bcastSocket;

    private InetSocketAddress broadcastAddress;

    private boolean shouldStop = true;

    private List<Peer> responseList = new ArrayList<Peer>();
    ;

    /**
     * Used to detect and ignore this peers response to it's own query.
     * When we send a response packet, we set this to the destination.
     * When we receive a response, if this matches the source, we know
     * that we're talking to ourselves and we can ignore the response.
     */
    private InetAddress lastResponseDestination = null;

    /**
     * Redefine this to be notified of exceptions on the listen thread.
     * Default behaviour is to print to stdout. Can be left as null for
     * no-op
     */
    public ExceptionHandler rxExceptionHandler = new ExceptionHandler();


    private Runnable bcastListen = new Runnable() {
        @Override
        public void run() {
            try {
                byte[] buffy = new byte[PACKET_SIZE];
                DatagramPacket rx = new DatagramPacket(buffy, buffy.length);

                boolean waitingEndJson = false;
                StringBuffer jsonString = null;
                while (!shouldStop) {
                    try {
                        System.out.println("Start");
                        buffy[0] = 0;
                        bcastSocket.receive(rx);

                        System.out.println("Recive");
                        int recData = decode(buffy, 1);


                        if (buffy[0] == QUERY_PACKET && recData == group) {
                            byte[] data = new byte[5];
                            data[0] = RESPONSE_PACKET;
                            encode(peerData, data, 1);

                            DatagramPacket tx =
                                    new DatagramPacket(data, data.length, rx.getAddress(), port);

                            lastResponseDestination = rx.getAddress();

                            bcastSocket.send(tx);
                            System.out.println("send");
                        } else if (buffy[0] == RESPONSE_PACKET) {
                            System.out.println("RESPONSE_PACKET");
//                            if (responseList != null && !rx.getAddress().equals(lastResponseDestination)) {
                            synchronized (responseList) {
                                responseList.add(new Peer(rx.getAddress(), recData));
                            }
//                            }
                        } else {
                            String message = new String(buffy, 0, rx.getLength());
                            System.out.println("Message : " + message);
                            if (callback != null) {
                                callback.response(message);
                            }
                        }
                    } catch (SocketException se) {
                        // someone may have called disconnect()
                        System.out.println("someone may have called disconnect()");
                    }
                }

                bcastSocket.disconnect();
                bcastSocket.close();
                System.out.println("close()");
            } catch (Exception e) {
                if (rxExceptionHandler != null) {
                    rxExceptionHandler.handle(e);
                }
            }
        }
    };
    private Thread bcastThread = null;

    public static PeerDiscovery getInstance(PeerDiscoverResponseInterface callback) {

        if (INSTANCE == null) {
            try {
                INSTANCE = new PeerDiscovery(PORT, PORT, callback);
            } catch (Exception e) {
                INSTANCE = null;
            }
        }

        return INSTANCE;
    }

    public boolean isRunning() {
        return !shouldStop;
    }

    /**
     * Constructs a UDP broadcast-based peer
     *
     * @param group    The identifier shared by the peers that will be
     *                 discovered.
     * @param port     a valid port, i.e.: in the range 1025 to 65535
     *                 inclusive
     * @param callback
     * @throws IOException
     */
    public PeerDiscovery(int group, int port, PeerDiscoverResponseInterface callback) throws IOException {
        this.group = group;
        this.port = port;
        this.callback = callback;
    }

    /**
     * Signals this {@link PeerDiscovery} to shut down. This call will
     * block until everything's timed out and closed etc.
     */
    public void disconnect() {
        shouldStop = true;

        bcastSocket.close();
        bcastSocket.disconnect();

        try {
            bcastThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Signals this {@link PeerDiscovery} to shut down. This call will
     * block until everything's timed out and closed etc.
     */
    public void connect() {
        shouldStop = false;

        try {
            bcastSocket = new DatagramSocket(port);
            broadcastAddress = new InetSocketAddress("255.255.255.255", port);


            bcastThread = new Thread(bcastListen);
            bcastThread.setDaemon(true);
            bcastThread.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }


    }

    /**
     * Queries the network and finds the addresses of other peers in
     * the same group
     *
     * @param timeout  How long to wait for responses, in milliseconds. Call
     *                 will block for this long, although you can
     *                 {@link Thread#interrupt()} to cut the wait short
     * @param peerType The type flag of the peers to look for
     * @return The addresses of other peers in the group
     * @throws IOException If something goes wrong when sending the query packet
     */
    public Peer[] getPeers(int timeout, byte peerType) throws IOException {
        responseList = new ArrayList<Peer>();

        // send query byte, appended with the group id
        byte[] data = new byte[5];
        data[0] = QUERY_PACKET;
        encode(group, data, 1);

        DatagramPacket tx = new DatagramPacket(data, data.length, broadcastAddress);

        bcastSocket.send(tx);

        // wait for the listen thread to do its thing
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
        }

        Peer[] peers;
        synchronized (responseList) {
            peers = responseList.toArray(new Peer[responseList.size()]);
        }

        return peers;
    }

    public void sendShareTab(Peer peer, String filename, InputStream content) throws IOException {

        JSONObject jSONObject = new JSONObject();
        jSONObject.put("command", "opentTab");
        jSONObject.put("filename", filename);
        jSONObject.put("filename", filename);


        String text = null;
        final Reader reader = new InputStreamReader(content);
        text = CharStreams.toString(reader);
        jSONObject.put("contents", text);

        byte[] bytes = jSONObject.toString().getBytes("UTF-8");
        System.out.println(bytes.length);
        sendPacket(new DatagramPacket(bytes, bytes.length, peer.ip, port));

//        byte sdata[] = new byte[1024];
//        int nRead = 0;
//        while ((nRead = content.read(sdata)) != -1) {
//            json.append(sdata);

//        }
    }

    private void sendPacket(DatagramPacket tx) {
        try {
            bcastSocket.send(tx);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Record of a peer
     *
     * @author ryanm
     */
    public class Peer {
        /**
         * The ip of the peer
         */
        public final InetAddress ip;

        /**
         * The data of the peer
         */
        public final int data;

        private Peer(InetAddress ip, int data) {
            this.ip = ip;
            this.data = data;
        }

        @Override
        public String toString() {
            return ip.getHostAddress();
        }
    }

    /**
     * Handles an exception.
     *
     * @author ryanm
     */
    public class ExceptionHandler {
        /**
         * Called whenever an exception is thrown from the listen
         * thread. The listen thread should now be dead
         *
         * @param e
         */
        public void handle(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            int group = PORT;

            PeerDiscovery mp = new PeerDiscovery(group, PORT, null);

            boolean stop = false;

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            while (!stop) {
                System.out.println("enter \"q\" to quit, or anything else to query peers");
                String s = br.readLine();

                if (s.equals("q")) {
                    System.out.print("Closing down...");
                    mp.disconnect();
                    System.out.println(" done");
                    stop = true;
                } else {
                    System.out.println("Querying");

                    Peer[] peers = mp.getPeers(100, (byte) 0);

                    System.out.println(peers.length + " peers found");
                    for (Peer p : peers) {
                        System.out.println("\t" + p);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int decode(byte[] b, int index) {
        int i = 0;

        i |= b[index] << 24;
        i |= b[index + 1] << 16;
        i |= b[index + 2] << 8;
        i |= b[index + 3];

        return i;
    }

    private static void encode(int i, byte[] b, int index) {
        b[index] = (byte) (i >> 24 & 0xff);
        b[index + 1] = (byte) (i >> 16 & 0xff);
        b[index + 2] = (byte) (i >> 8 & 0xff);
        b[index + 3] = (byte) (i & 0xff);
    }
}
