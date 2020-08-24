package gitlet;

import java.io.Serializable;
import java.util.ArrayList;

public class Branch implements Serializable {

    private final String name;
    private final String start; // Path to first Commit
    private String current; // Path to latest Commit
    private ArrayList<String> commits; // Paths to all Commits

    public Branch(String n, String s) {
        name = n;
        start = s;
        current = s;
        commits = new ArrayList<>();

        String currBranchPath = Mytils.getCurrentBranch();
        Branch currBranch = (Branch) Mytils.writeIn(currBranchPath);
        ArrayList<String> currBranchCommits = currBranch.getCommits();
        commits.addAll(currBranchCommits);
    }

    /* Constructor specifically for the master branch */
    public Branch(String hashCommit) {
        name = "master";
        start = hashCommit;
        current = hashCommit;
        commits = new ArrayList<>();
        commits.add(hashCommit);
    }

    public ArrayList<String> getCommits() {
        return commits;
    }

    public String getCurrent() {
        return current;
    }

    public void changeCurrent(String newCurr) {
        commits.add(newCurr);
        current = newCurr;
    }

    public String getStart() {
        return start;
    }

    public void addToCommits(String commit) {
        commits.add(commit);
    }

}
