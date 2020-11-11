package cs451;

import java.util.*;

public class RB implements Runnable {

    private long pid;
    private int id;
    private List<Host> hosts;

    private static Map<Integer, Set<Message>> receivedFrom;
    private static Set<Message> delivered;
    private static Set<Integer> correct; //Set<long> maybe???

    public RB(long pid, int id, List<Host> hosts) {
        this.pid = pid;
        this.id = id;
        this.hosts = hosts;

        this.receivedFrom = new HashMap<>();
        this.correct = new HashSet<>();

        for (Host host : hosts) {
            receivedFrom.put(host.getId(), new HashSet<>());
            correct.add(host.getId());
        }

        this.delivered = new HashSet<>();
    }

    @Override
    public void run() {
        new Thread(new BEB(pid, id, hosts)).start();
    }

    public static void broadcast(Message msg) {
        delivered.add(msg);
        //here deliver it higher up, e.x. URB.deliver(msg)
        BEB.broadcast(msg);
    }

    public static void deliver(Message msg) {
        if (!delivered.contains(msg)) {
            delivered.add(msg);
            if (!correct.contains(msg.getSender_id())) {
                BEB.broadcast(msg);
            } else {
                receivedFrom.get(msg.getSender_id()).add(msg);
            }
        }
        Main.logMessage(msg);
    }

    public static void onCrash(int pi) {
        correct.remove(pi);
        for (Message msg : receivedFrom.get(pi)) {
            BEB.broadcast(msg);
        }
    }
}
