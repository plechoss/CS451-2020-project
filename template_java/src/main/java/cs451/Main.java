package cs451;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {
    private static List<Message> delivered = new ArrayList<>();

    private static void writeDeliveredMessages(){

    }

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
        //writeDeliveredMessages();
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void logMessage(Message msg) {
        String line = "received " + msg.getSeq_nr() + " from " + msg.getSender_id() + "\n";

        String logName = "logMain" + ProcessHandle.current().pid();
        Logger logger = Logger.getLogger(logName);
        FileHandler fh;

        try {
            // This block configure the logger with handler and formatter
            fh = new FileHandler("/Users/michal/Desktop/logs/" + logName, true);
            logger.addHandler(fh);

            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }

        logger.info(line);
    }

    public static void deliver(Message msg){
        delivered.add(msg);
    }

    public static boolean isRunning() {
        return true;
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();

        System.out.println("My PID is " + pid + ".");
        System.out.println("Use 'kill -SIGINT " + pid + " ' or 'kill -SIGTERM " + pid + " ' to stop processing packets.");

        System.out.println("My id is " + parser.myId() + ".");
        System.out.println("List of hosts is:");
        for (Host host : parser.hosts()) {
            System.out.println(host.getId() + ", " + host.getIp() + ", " + host.getPort());
        }

        System.out.println("Barrier: " + parser.barrierIp() + ":" + parser.barrierPort());
        System.out.println("Signal: " + parser.signalIp() + ":" + parser.signalPort());
        System.out.println("Output: " + parser.output());
        // if config is defined; always check before parser.config()
        if (parser.hasConfig()) {
            System.out.println("Config: " + parser.config());
        }


        Coordinator coordinator = new Coordinator(parser.myId(), parser.barrierIp(), parser.barrierPort(), parser.signalIp(), parser.signalPort());

        System.out.println("Waiting for all processes for finish initialization");
        coordinator.waitOnBarrier();

        System.out.println("Broadcasting messages...");

        new Thread(new FIFO(pid, parser.myId(), parser.hosts())).start();
        int[] vc = new int[parser.hosts().size()];

        for (int i = 0; i < parser.hosts().size(); i++) {
            vc[i] = 0;
        }

        for (int i = 0; i < 10; i++) {
            FIFO.broadcast(new Message(i, parser.myId(), parser.myId(), vc));
        }

        System.out.println("Signaling end of broadcasting messages");
        coordinator.finishedBroadcasting();

        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
