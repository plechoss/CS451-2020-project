package cs451;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FIFO implements Runnable {

    private long pid;
    private static int id;
    private static List<Host> hosts;
    private String barrierIP;
    private int barrierPort;
    private String signalIP;
    private int signalPort;

    private static Set<Integer> correct; //Set<long> maybe???
    private static Set<Message> pending;
    private static int[] vc;

    public FIFO(long pid, int id, List<Host> hosts, String barrierIP, int barrierPort, String signalIP, int signalPort) {
        this.pid = pid;
        this.id = id;
        this.hosts = hosts;
        this.barrierIP = barrierIP;
        this.barrierPort = barrierPort;
        this.signalIP = signalIP;
        this.signalPort = signalPort;

        this.correct = new HashSet<>();
        for (Host host : hosts) {
            correct.add(host.getId());
        }

        this.vc = new int[correct.size()];
        for (int i = 0; i < correct.size(); i++) {
            vc[i] = 0;
        }
        this.pending = new HashSet<>();
    }

    @Override
    public void run() {
        new Thread(new URB(pid, id, hosts, barrierIP, barrierPort, signalIP, signalPort)).start();
    }

    public static void broadcast(Message msg) {
        Main.deliver(msg);
        URB.broadcast(msg);
        vc[id]++;
    }

    public static void deliver(Message msg) {
        if (msg.getCreator_id() != id) {
            pending.add(msg);
            //deliver-pending stuff
            boolean keepGoing = true;
            while (keepGoing) {
                for (Message m : pending) {
                    boolean canDeliverMessage = true;
                    int[] msg_vc = m.getVector_clock();
                    for (int i = 0; i < hosts.size(); i++) {
                        if (vc[i] < msg_vc[i]){
                            canDeliverMessage = false;
                            break;
                        }
                    }
                    if(canDeliverMessage){
                        pending.remove(m);
                        Main.deliver(msg);
                        vc[m.getCreator_id()]++;
                    }
                }
            }
        }
    }
}
