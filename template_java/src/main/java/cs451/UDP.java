package cs451;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UDP implements Runnable {
    private long pid;
    private static int id;
    private static List<Host> hosts;
    private String barrierIP;
    private int barrierPort;
    private String signalIP;
    private int signalPort;

    private static DatagramSocket socket;
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
    }

    //send one message to all the available hosts
    //should be good now
    public static void broadcast(Message msg) throws java.lang.InterruptedException {
        while (true) {
            try {
                socket = new DatagramSocket();
                address = InetAddress.getByAddress(hosts.get(id).getIp().getBytes());

                byte[] buf = msg.toString().getBytes();
                DatagramPacket packet;
                for (Host host : hosts) {
                    InetAddress ip = InetAddress.getByAddress(host.getIp().getBytes());
                    packet = new DatagramPacket(buf, buf.length, ip, host.getPort());
                    socket.send(packet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            TimeUnit.SECONDS.sleep(1);
        }
    }

    @Override
    public void run() {
        byte[] buf = new byte[65535];

        while (running) {
            try {
                while (Main.isRunning()) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);

                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();
                    packet = new DatagramPacket(buf, buf.length, address, port);
                    String payload
                            = new String(packet.getData(), 0, packet.getLength());

                    Main.logMessage(new Message(payload));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                socket.close();
            }
        }
        socket.close();
    }
}
