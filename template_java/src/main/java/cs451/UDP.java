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

    private static DatagramSocket socket;
    private static int port;
    private static InetAddress address;

    private boolean running;

    public UDP(long pid, int id, List<Host> hosts) {
        this.pid = pid;
        this.id = id;
        this.hosts = hosts;

        this.port = hosts.get(id-1).getPort();
        try {
            this.address = InetAddress.getByName(hosts.get(id-1).getIp());
            this.socket = new DatagramSocket(this.port, this.address);
        } catch (Exception e) {
            System.out.println("Error in setting socket in UDP constructor");
            System.out.println(e.toString());
        }
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
            System.out.println("Error in broadcast()");
            System.out.println(e.toString());
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
            System.out.println("Error in run()");
            System.out.println(e.toString());
        } finally {
            socket.close();
        }
    }
}
