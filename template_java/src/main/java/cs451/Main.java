package cs451;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        FIFO.shutdown();

        //write/flush output file if necessary
        System.out.println("Writing output.");
        FIFO.writeDeliveredMessages();
        //System.out.println(delivered);
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException, IOException {
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

        //https://stackoverflow.com/questions/24666805/java-only-read-first-line-of-a-file
        BufferedReader configReader = new BufferedReader(new FileReader(parser.config()));
        String firstLine = configReader.readLine();
        int num_messages = Integer.parseInt(firstLine);

        Coordinator coordinator = new Coordinator(parser.myId(), parser.barrierIp(), parser.barrierPort(), parser.signalIp(), parser.signalPort());

        System.out.println("Waiting for all processes for finish initialization");
        coordinator.waitOnBarrier();

        System.out.println("Broadcasting messages...");

        new Thread(new FIFO(pid, parser.myId(), parser.hosts())).start();

        if (FIFO.initWriter(parser.output()) == -1) {
            System.out.println("Writer error, can't initialize printWriter");
        }

        int[] vc = new int[parser.hosts().size()];

        for (int i = 0; i < parser.hosts().size(); i++) {
            vc[i] = 0;
        }

        for (int i = 1; i < num_messages + 1; i++) {
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
