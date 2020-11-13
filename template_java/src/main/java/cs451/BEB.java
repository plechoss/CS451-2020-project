package cs451;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

import static java.lang.Thread.sleep;

//mostly based on https://www.baeldung.com/udp-in-java
public class BEB implements Runnable {
    private static long pid;
    private static int id;
    private static List<Host> hosts;

    public BEB(long pid, int id, List<Host> hosts) {
        this.pid = pid;
        this.id = id;
        this.hosts = hosts;
    }

    public static void broadcast(Message msg) {
        Sender.broadcast(msg);
    }

    public static void deliver(Message msg) {
        URB.deliver(msg);
    }

    @Override
    public void run() {
        new Thread(new Sender(pid, id, hosts)).start();
        new Thread(new Receiver(pid, id, hosts)).start();
    }
}
