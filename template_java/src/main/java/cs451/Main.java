package cs451;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Main {

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        System.out.println("Shutdown at: " + System.currentTimeMillis()/1000 + "s");
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
        //create output file
        File file = new File(parser.output());

        //https://examples.javacodegeeks.com/core-java/io/file/create-new-empty-file/
        try {
            file.createNewFile();
        }
        catch (IOException ioe) {
            System.out.println("Error while creating empty file: " + ioe);
        }

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



        int line_num = 1;
        Set<Integer> dependencies = new HashSet<Integer>();

        for (String line = configReader.readLine(); line != null; line = configReader.readLine()) {
            if(line_num == parser.myId()){
                String[] split = line.split("\\s+");
                for(String dependency : split){
                    dependencies.add(Integer.parseInt(dependency));
                }
                break;
            }
            line_num++;
        }

        Coordinator coordinator = new Coordinator(parser.myId(), parser.barrierIp(), parser.barrierPort(), parser.signalIp(), parser.signalPort());

        System.out.println("Waiting for all processes for finish initialization");
        coordinator.waitOnBarrier();

        System.out.println("Broadcasting messages...");

        new Thread(new CB(pid, parser.myId(), parser.hosts(), dependencies)).start();
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
