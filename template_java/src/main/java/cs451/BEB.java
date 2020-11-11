package cs451;

import java.util.List;

public class BEB implements Runnable {

    private long pid;
    private int id;
    private List<Host> hosts;

    public BEB(long pid, int id, List<Host> hosts) {
        this.pid = pid;
        this.id = id;
        this.hosts = hosts;
    }

    @Override
    public void run() {
        new Thread(new UDP(pid, id, hosts)).start();
    }

    public static void broadcast(Message msg) {
        UDP.broadcast(msg);
    }

    public static void deliver(Message msg) {
        URB.deliver(msg);
    }
}
