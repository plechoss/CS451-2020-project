package cs451;

import java.util.*;

public class URB implements Runnable {

    private long pid;
    private static int id;
    private static List<Host> hosts;

    private static Map<Message, Set<Integer>> acks;
    private static Set<Message> delivered;
    private static Set<Message> forward;

    public URB(long pid, int id, List<Host> hosts) {
        this.pid = pid;
        this.id = id;
        this.hosts = hosts;

        this.acks = new HashMap<>();
        this.delivered = new HashSet<>();
        this.forward = new HashSet<>();
    }

    @Override
    public void run() {
        new Thread(new BEB(pid, id, hosts)).start();
        while (true) {
            try {
                wait(200);
                for (Message msg : forward) {
                    if (canDeliver(msg) && !delivered.contains(msg)) {
                        delivered.add(msg);

                        //deliver the message higher up
                        FIFO.deliver(msg);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void broadcast(Message msg) { //DONE
        forward.add(msg);
        BEB.broadcast(msg);
    }

    public static void deliver(Message msg) { //DONE
        if (acks.containsKey(msg)) {
            acks.get(msg).add(msg.getSender_id());
        } else {
            acks.put(msg, new HashSet<>(msg.getSender_id()));
        }
        if (!forward.contains(msg)) {
            forward.add(msg);
            BEB.broadcast(new Message(msg.getSeq_nr(), msg.getCreator_id(), id, msg.getVector_clock()));
        }
    }

    public static boolean canDeliver(Message msg) {
        int hosts_size = hosts.size();
        int ack_size = acks.get(msg).size();

        return ack_size > hosts_size / 2;
    }
}
