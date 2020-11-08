package cs451;

import java.util.*;

public class URB implements Runnable {

    private long pid;
    private static int id;
    private List<Host> hosts;
    private String barrierIP;
    private int barrierPort;
    private String signalIP;
    private int signalPort;

    private static Map<Message, Set<Integer>> acks;
    private static Set<Message> delivered;
    private static Set<Message> forward;
    private static Set<Integer> correct; //Set<long> maybe???

    public URB(long pid, int id, List<Host> hosts, String barrierIP, int barrierPort, String signalIP, int signalPort) {
        this.pid = pid;
        this.id = id;
        this.hosts = hosts;
        this.barrierIP = barrierIP;
        this.barrierPort = barrierPort;
        this.signalIP = signalIP;
        this.signalPort = signalPort;

        this.acks = new HashMap<>();
        this.correct = new HashSet<>();

        for (Host host : hosts) {
            correct.add(host.getId());
        }

        this.delivered = new HashSet<>();
        this.forward = new HashSet<>();
    }

    @Override
    public void run() {
        new Thread(new BEB(pid, id, hosts, barrierIP, barrierPort, signalIP, signalPort)).start();
        while (true) {
            try {
                wait(200);
                for (Message msg : forward) {
                    if(acks.get(msg).containsAll(correct) && !delivered.contains(msg)) {
                        delivered.add(msg);

                        //deliver the message higher up
                        Main.logMessage(msg);
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
        if(acks.containsKey(msg)){
            acks.get(msg).add(msg.getSender_id());
        } else {
            acks.put(msg, new HashSet<>(msg.getSender_id()));
        }
        if (!forward.contains(msg)) {
            forward.add(msg);
            BEB.broadcast(new Message(msg.getSeq_nr(), msg.getCreator_id(), id));
        }
    }

    public static void onCrash(int pi) { //DONE
        correct.remove(pi);
    }
}
