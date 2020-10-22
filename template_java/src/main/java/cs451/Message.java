package cs451;

import java.io.Serializable;

public class Message implements Serializable {
    private int seq_nr;
    private boolean is_ack;
    private boolean ack_from;
    private int sender_id;

    public Message(int seq_nr, boolean is_ack, boolean ack_from, int sender_id) {
        this.seq_nr = seq_nr;
        this.is_ack = is_ack;
        this.ack_from = ack_from;
        this.sender_id = sender_id;
    }

    public int getSeq_nr() {
        return seq_nr;
    }

    public boolean isIs_ack() {
        return is_ack;
    }

    public boolean isAck_from() {
        return ack_from;
    }

    public int getSender_id() {
        return sender_id;
    }
}
