package cs451;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FIFO implements Runnable {

    private long pid;
    private static int id;
    private static List<Host> hosts;

    private static ConcurrentHashMap<Message, Boolean> pending;
    private static int[] vc;

    public FIFO(long pid, int id, List<Host> hosts) {
        this.pid = pid;
        this.id = id;
        this.hosts = hosts;

        this.vc = new int[hosts.size()];
        for (int i = 0; i < hosts.size(); i++) {
            vc[i] = 0;
        }
        this.pending = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        new Thread(new URB(pid, id, hosts)).start();
    }

    public static void broadcast(Message msg) {
        Message new_message = new Message(msg.getSeq_nr(), msg.getCreator_id(), msg.getSender_id(), vc);
        Main.deliver(new_message);
        URB.broadcast(new_message);
        vc[id-1]++;
    }

    public static void deliver(Message msg) {
        if (msg.getCreator_id() != id) {
            pending.put(msg, true);
            //deliver-pending stuff
            boolean keepGoing = true;
            while (keepGoing) {
                keepGoing = false;
                for (Message m : pending.keySet()) {
                    boolean canDeliverMessage = true;
                    int[] msg_vc = m.getVector_clock();
                    for (int i = 0; i < hosts.size(); i++) {
                        if (vc[i] < msg_vc[i]) {
                            canDeliverMessage = false;
                            break;
                        }
                    }
                    if (canDeliverMessage) {
                        pending.remove(m);
                        System.out.println("Main delivering in FIFO:");
                        System.out.println(msg);
                        Main.deliver(msg);
                        vc[m.getCreator_id()-1]++;
                        keepGoing = true;
                    }
                }
            }
        }
    }
}
