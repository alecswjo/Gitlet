package gitlet;

import java.io.Serializable;
import java.util.ArrayList;

public class BranchArray implements Serializable {

    private ArrayList<String> branches; // Array of branch Paths

    public BranchArray() {
        branches = new ArrayList();
    }

    public void add(String branchPath) {
        branches.add(branchPath);
    }

    public void remove(String branchPath) {
        branches.remove(branchPath);
    }

    public ArrayList<String> getArray() {
        return branches;
    }
}
