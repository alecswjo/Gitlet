package gitlet;

import java.io.Serializable;


public class Head implements Serializable {

    private String branch; // path of current Branch

    /* Constructor taking in path of a Branch */
    public Head(String b) {
        branch = b;
    }

    public String getBranch() {
        return branch;
    }

    public void changeBranch(String b) {
        branch = b;
    }
}
