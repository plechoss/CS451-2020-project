package cs451;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Thread.sleep;

public class URB implements Runnable {

    private long pid;
    private static int id;
    private static List<Host> hosts;

    private static ConcurrentHashMap<Message, Set<Integer>> acks;
    private static ConcurrentHashMap<Message, Boolean> delivered;
    private static ConcurrentHashMap<Message, Boolean> forward;

    public URB(long pid, int id, List<Host> hosts) {
        this.pid = pid;
        this.id = id;
        this.hosts = hosts;

        this.acks = new ConcurrentHashMap<>();
        this.delivered = new ConcurrentHashMap<>();
        this.forward = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        new Thread(new BEB(pid, id, hosts)).start();
        while (true) {
            try {
                for (Message msg : forward.keySet()) {
                    if (canDeliver(msg) && !delivered.containsKey(msg)) {
                        delivered.put(msg, true);

                        //deliver the message higher up
                        FIFO.deliver(msg);
                    }
                }
                sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void broadcast(Message msg) { //DONE
        forward.put(msg, true);
        BEB.broadcast(msg);
    }

    public static void deliver(Message msg) { //DONE
        if (acks.containsKey(msg)) {
            acks.get(msg).add(msg.getSender_id());
        } else {
            acks.put(msg, new HashSet<>(msg.getSender_id()));
        }
        if (!forward.contains(msg)) {
            forward.put(msg, true);
            BEB.broadcast(new Message(msg.getSeq_nr(), msg.getCreator_id(), id, msg.getVector_clock()));
        }
    }

    public static boolean canDeliver(Message msg) {
        int hosts_size = hosts.size();
        int ack_size = acks.getOrDefault(msg, new HashSet<>()).size();

        return ack_size >= hosts_size / 2;
    }
}
