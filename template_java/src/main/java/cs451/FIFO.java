package cs451;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Thread.sleep;

public class FIFO implements Runnable {
    private static boolean shutdown;
    private long pid;
    private static int id;
    private static List<Host> hosts;

    //https://stackoverflow.com/a/25630263/14410028
    private static Queue<Message> delivered = new ConcurrentLinkedQueue<>();

    private static ConcurrentHashMap<Message, Boolean> pending;
    private static int[] vc;

    private static PrintWriter writer;

    public FIFO(long pid, int id, List<Host> hosts) {
        this.pid = pid;
        this.id = id;
        this.hosts = hosts;
        this.shutdown = false;

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
        if (!shutdown) {
            Message new_message = new Message(msg.getSeq_nr(), msg.getCreator_id(), msg.getSender_id(), vc);
            //System.out.println("Constructing a new messsage with seq_nr: " + new_message.getSeq_nr() + ", creator: " + new_message.getCreator_id() + " and vc: " + vc)
            try {
                sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            delivered.add(new_message);
            URB.broadcast(new_message);
            vc[id - 1]++;
        }
    }

    public static void shutdown() {
        shutdown = true;
    }

    public static void deliver(Message msg) {
        if (!shutdown) {
            if (msg.getCreator_id() != id && !pending.containsKey(msg)) {
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
                            delivered.add(m);
                            vc[m.getCreator_id() - 1]++;
                            keepGoing = true;
                            pending.remove(m);
                        }
                    }
                }
            }
        }
    }

    public static int initWriter(String path) {
        try {
            writer = new PrintWriter(new FileWriter(path));
        } catch (Exception e) {
            return -1;
        }
        return 1;
    }

    public static void writeDeliveredMessages() {
        String line = "";
        for (Message msg : delivered) {
            if (msg.getCreator_id() == id) {
                line = "b " + msg.getSeq_nr() + "\n";
            } else {
                line = "d " + +msg.getCreator_id() + " " + msg.getSeq_nr() + "\n";
            }
            writer.write(line);
        }
        writer.close();
    }
}
