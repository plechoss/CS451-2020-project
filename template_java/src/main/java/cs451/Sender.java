package cs451;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Thread.sleep;

public class Sender implements Runnable {
    private static long pid;
    private static int id;
    private static List<Host> hosts;

    private static DatagramSocket socket;

    private static Queue<Message> message_queue = new ConcurrentLinkedQueue<>();

    public Sender(long pid, int id, List<Host> hosts) {
        this.pid = pid;
        this.id = id;
        this.hosts = hosts;

        try {
            this.socket = new DatagramSocket();
        } catch (Exception e) {
            System.out.println("Error in setting socket in Sender constructor");
            System.out.println(e.toString());
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (Message msg : message_queue) {
                sendToAllHosts(msg);
            }
        }
    }

    public static void broadcast(Message msg) {
        message_queue.add(msg);
    }

    public static void stopBroadcasting(Message msg){
        message_queue.remove(msg);
    }

    public static void sendToAllHosts(Message msg) {
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
}
