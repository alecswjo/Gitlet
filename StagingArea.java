package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class StagingArea implements Serializable {

    public HashMap<String, String> stagedFiles = new HashMap<>(); // Name:Path
    public HashMap<String, String> trackedFiles = new HashMap<>(); // Name:Path
    public HashMap<String, String> removedFiles = new HashMap<>(); // Name:Path
    public HashMap<String, String> untrackedFiles = new HashMap<>(); // Name:Path
    public HashSet<String> lostCommits = new HashSet<>(); // Path


}
