package cs451;

import java.io.Serializable;

public class Message implements Serializable {
    private int seq_nr;
    private boolean is_ack;
    private int ack_from;
    private int sender_id;

    public Message(int seq_nr, boolean is_ack, int ack_from, int sender_id) {
        this.seq_nr = seq_nr;
        this.is_ack = is_ack;
        this.ack_from = ack_from;
        this.sender_id = sender_id;
    }

    public Message(String payload) {
        String[] fields = payload.split(",", 4);

        this.seq_nr = Integer.parseInt(fields[0]);
        this.is_ack = Boolean.parseBoolean(fields[1]);
        this.ack_from = Integer.parseInt(fields[2]);
        this.sender_id = Integer.parseInt(fields[3]);
    }

    public int getSeq_nr() {
        return seq_nr;
    }

    public boolean isIs_ack() {
        return is_ack;
    }

    public int getAck_from() {
        return ack_from;
    }

    public int getSender_id() {
        return sender_id;
    }

    public String toString() {
        return seq_nr + "," + is_ack + "," + ack_from + "," + sender_id;
    }
}
