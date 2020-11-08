package cs451;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//mostly based on https://www.baeldung.com/udp-in-java
public class UDP implements Runnable {
    private static long pid;
    private static int id;
    private static List<Host> hosts;
    private String barrierIP;
    private int barrierPort;
    private String signalIP;
    private int signalPort;

    private static DatagramSocket socket;
    private static int port;
    private static InetAddress address;

    private boolean running;

    public UDP(long pid, int id, List<Host> hosts, String barrierIP, int barrierPort, String signalIP, int signalPort) {
        this.pid = pid;
        this.id = id;
        this.hosts = hosts;
        this.barrierIP = barrierIP;
        this.barrierPort = barrierPort;
        this.signalIP = signalIP;
        this.signalPort = signalPort;

        this.port = hosts.get(id).getPort();
        try {
            this.address = InetAddress.getByName(hosts.get(id).getIp());
            this.socket = new DatagramSocket(this.port, this.address);
        } catch (Exception e) {
            logMessage(e.toString());
        }
    }

    public static void logMessage(String msg) {
        String logName = "logUDP" + pid;
        Logger logger = Logger.getLogger(logName);
        FileHandler fh;

        try {
            // This block configures the logger with handler and formatter
            fh = new FileHandler("/Users/michal/Desktop/logs/" + logName, true);
            logger.addHandler(fh);

            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        logger.info(msg);
    }

    public static void broadcast(Message msg){
        try {
            byte[] buf = msg.toString().getBytes();
            DatagramPacket packet;
            for (Host host : hosts) {
                InetAddress ip = InetAddress.getByName(host.getIp());
                packet = new DatagramPacket(buf, buf.length, ip, host.getPort());
                socket.send(packet);
            }
        } catch (Exception e) {
            logMessage("Error in broadcast()");
            logMessage(e.toString());
        }
    }

    @Override
    public void run() {
        byte[] buf = new byte[65535];
        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            while (Main.isRunning()) {
                socket.receive(packet);

                String payload = new String(packet.getData(), 0, packet.getLength());

                BEB.deliver(new Message(payload));
            }
        } catch (IOException e) {
            logMessage("Error in run()");
            logMessage(e.toString());
        } finally {
            socket.close();
        }
    }
}
