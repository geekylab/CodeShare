package net.geekylab.codeshare.network.tcp;

import net.geekylab.codeshare.network.ReceivedInterface;
import net.geekylab.codeshare.network.Transport;
import net.geekylab.codeshare.network.TransportListener;

import java.io.*;

/**
 * Created by johna on 13/09/14.
 */
public class ShareCodeTcpServer implements TransportListener {
    private OutputStream outputStream = null;
    private boolean running;
    private TCPServerTransport tcpServerTransport;
    private ReceivedInterface callback;

    public ShareCodeTcpServer(OutputStream outputStream) {

        this.outputStream = outputStream;
    }

    public ShareCodeTcpServer() {

    }


    public static void main(String[] args) throws IOException {
        new ShareCodeTcpServer().start();
    }

    public void start() throws IOException {
        tcpServerTransport = new TCPServerTransport("0.0.0.0", 12033, this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    tcpServerTransport.accept();
                } catch (IOException e) {
                }
            }
        }).start();

    }

    @Override
    public void onReceivedFromTransport(Transport transport, String message) {
        if (this.callback != null) {
            this.callback.onReceived(message);
        }
    }

    @Override
    public void onConnected(Transport transport) {
        System.out.println("connected");
        if (this.outputStream != null) {
            InputStream in = new BufferedInputStream(transport.getInputStream());
            OutputStream out = new BufferedOutputStream(this.outputStream);

            byte[] buffer = new byte[8192];
            int count;
            try {
                while ((count = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, count);
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisconnected(Transport transport) {
        System.out.println("Disconneted Connected!!");
    }

    public boolean isRunning() {
        return tcpServerTransport != null && tcpServerTransport.isRunning();
    }

    public void stop() throws IOException {
        if (tcpServerTransport != null) {
            tcpServerTransport.close();
        }
    }

    public void setReceivedListener(ReceivedInterface callback) {
        this.callback = callback;
    }
}
