package cs451;

import java.io.Serializable;
import java.util.Arrays;

public class Message implements Serializable {
    private int seq_nr;
    private int creator_id;
    private int sender_id;
    private int[] vc;

    public Message(int seq_nr, int creator_id, int sender_id, int[] vc) {
        this.seq_nr = seq_nr;
        this.creator_id = creator_id;
        this.sender_id = sender_id;
        this.vc = Arrays.copyOf(vc, vc.length);
    }

    public Message(String payload) {
        String[] fields = payload.split(",", 4);
        this.seq_nr = Integer.parseInt(fields[0]);
        this.creator_id = Integer.parseInt(fields[1]);
        this.sender_id = Integer.parseInt(fields[2]);

        //watch out for first value being "" after split
        String[] string_vc = fields[3].split("\\.");
        this.vc = new int[string_vc.length-1];
        for (int i = 0; i < string_vc.length-1; i++) {
            this.vc[i] = Integer.parseInt(string_vc[i]);
        }
        System.out.println("Vector clock after parsing msg from payload:");
        System.out.println(this.vc);
    }

    public int getSeq_nr() {
        return seq_nr;
    }

    public int getCreator_id() {
        return creator_id;
    }

    public int getSender_id() {
        return sender_id;
    }

    public int[] getVector_clock() {
        return vc;
    }

    public String toString() {
        String vc_string = ",";
        for (int i = 0; i < this.vc.length; i++) {
            vc_string = vc_string + this.vc[i];
            if (i != this.vc.length - 1) {
                vc_string = vc_string + ".";
            }
        }

        return seq_nr + "," + creator_id + "," + sender_id + vc_string;
    }
}
