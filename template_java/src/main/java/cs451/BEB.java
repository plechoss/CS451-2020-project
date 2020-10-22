package cs451;

import java.util.List;

public class BEB {

    private long pid;
    private int id;
    private List<Host> hosts;
    private String barrierIP;
    private int barrierPort;
    private String signalIP;
    private int signalPort;

    public BEB(long pid, int id, List<Host> hosts, String barrierIP,
               int barrierPort, String signalIP, int signalPort) {
        this.pid = pid;
        this.id = id;
        this.hosts = hosts;
        this.barrierIP = barrierIP;
        this.barrierPort = barrierPort;
        this.signalIP = signalIP;
        this.signalPort = signalPort;
    }

    private void startReceiving(){

    }
}
