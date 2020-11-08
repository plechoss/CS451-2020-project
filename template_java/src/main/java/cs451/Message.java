package cs451;

import java.io.Serializable;

public class Message implements Serializable {
    private int seq_nr;
    private int creator_id;
    private int sender_id;

    public Message(int seq_nr, int creator_id, int sender_id) {
        this.seq_nr = seq_nr;
        this.creator_id = creator_id;
        this.sender_id = sender_id;
    }

    public Message(String payload) {
        String[] fields = payload.split(",", 3);

        this.seq_nr = Integer.parseInt(fields[0]);
        this.creator_id = Integer.parseInt(fields[1]);
        this.sender_id = Integer.parseInt(fields[2]);
    }

    public int getSeq_nr() {
        return seq_nr;
    }

    public int getCreator_id() { return creator_id; }

    public int getSender_id() {
        return sender_id;
    }

    public String toString() {
        return seq_nr + "," + creator_id + "," + sender_id;
    }
}
