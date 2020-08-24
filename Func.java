package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Func {

    /* Create initial Commit, StagingArea, and appropriate Pointers */
    public static void init() {
        if (new File(".gitlet").exists()) {
            System.out.print("A gitlet version-control system "
                    + "already exists in the current directory.");
            return;
        }
        new File(".gitlet").mkdir();
        Commit firstCommit = new Commit();
        String commitPath = Mytils.serialize(firstCommit);

        Branch master = new Branch(commitPath);
        String masterPath = Mytils.serialize(master, "master");

        Head head = new Head(masterPath);
        Mytils.serialize(head, "HEAD");

        StagingArea stagingArea = new StagingArea();
        Mytils.serialize(stagingArea, "StagingArea");

        BranchArray branchArray = new BranchArray();
        branchArray.add(masterPath);
        Mytils.serialize(branchArray, "BranchArray");
    }

    public static void add(String fileName) {
        if (!new File(System.getProperty("user.dir") + "/" + fileName).exists()) {
            System.err.println("File does not exist.");
            return;
        }
        SAHelper.addStagedFile(fileName);
    }

    public static void commit(String message) {
        if (message == null) {
            System.out.println("Please enter a commit message.");
            return;
        }
        if (SAHelper.peekStagedFiles().isEmpty() && SAHelper.peekRemovedFiles().isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        Commit newCommit = new Commit(message);
        String commitPath = Mytils.serialize(newCommit);
        Head h = (Head) Mytils.writeIn(".gitlet/HEAD.txt");
        String hb = h.getBranch();
        Branch b = (Branch) Mytils.writeIn(hb);
        b.changeCurrent(commitPath);
        new File(hb).delete();
        String branch = Mytils.serialize(b, Mytils.strip(hb));
    }

    public static void remove(String fileName) {
        if (!SAHelper.removeStagedFile(fileName)) {
            System.out.println("No reason to remove the file.");
        }
        return;
    }

    public static void log() {
        String c = Mytils.getCurrentCommit();
        Commit p = (Commit) Mytils.writeIn(c);
        while (p != null) {
            System.out.println("===");
            System.out.println("Commit " + Mytils.strip(c));
            System.out.println(p.getTime());
            System.out.println(p.getMessage());
            System.out.println();
            c = p.getParent();
            p = (Commit) Mytils.writeIn(c);
        }
    }

    public static void checkout1(String fileName) {
        HashMap<String, String> trackedFiles = SAHelper.start().trackedFiles;
        if (!trackedFiles.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        File blob = new File(trackedFiles.get(fileName)); //makes a new file with the textfile
        try {
            Files.copy(blob.toPath(), file.toPath());
        } catch (IOException e) {
            System.out.println("copy error");
            return;
        }
    }

    public static void checkout2(String commitID, String fileName) {
        if (!new File(commitID).exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit = (Commit) Mytils.writeIn(commitID);
        HashMap<String, String> dict = commit.getDict();
        if (!dict.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        new File(fileName).delete();
        File blob = new File(dict.get(fileName));
        File overwrite = new File(fileName);
        try {
            Files.copy(blob.toPath(), overwrite.toPath());
        } catch (IOException e) {
            System.out.println("copy error");
        }
    }

    public static void checkout3(String branchName) {
        if (!new File(Mytils.path(branchName)).exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        if (Mytils.path(branchName).equals(Mytils.getCurrentBranch())) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        String otherCommitPath = Mytils.getCurrentCommit(Mytils.path(branchName));
        Commit otherCommit = (Commit) Mytils.writeIn(otherCommitPath);
        HashMap<String, String> oDict = otherCommit.getDict();
        ArrayList<String> allUntracked = Mytils.getAllUntrackedFiles();
        for (String fileName : oDict.keySet()) {
            if (allUntracked
                    .contains(fileName)) {
                System.out.println("There is an untracked file "
                        +
                        "in the way; delete it or add it first.");
                return;
            }
        }
        String currCommitPath = Mytils.getCurrentCommit();
        Commit currCommit = (Commit) Mytils.writeIn(currCommitPath);
        HashMap<String, String> currTrackedFiles = currCommit.getDict();
        for (String trackedFileName : currTrackedFiles.keySet()) {
            if (!oDict.containsKey(trackedFileName)) {
                File file = new File(System.getProperty("user.dir") + "/" + trackedFileName);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        for (String fileName : oDict.keySet()) {
            File file = new File(System.getProperty("user.dir") + "/" + fileName);
            if (file.exists()) {
                file.delete();
            }
            File insertBlob = new File(oDict.get(fileName));
            File overwrite = new File(System.getProperty("user.dir") + "/" + fileName);
            try {
                Files.copy(insertBlob.toPath(), overwrite.toPath());
            } catch (IOException e) {
                System.out.println("copy error");
            }
        }
        Head head = (Head) Mytils.writeIn(Mytils.path("HEAD"));
        head.changeBranch(Mytils.path(branchName));
        Mytils.serialize(head, "HEAD");
        SAHelper.newHead();
        return;
    }

    public static void status() {
        //prints out Branches Status
        System.out.println("=== Branches ===");
        BranchArray array = (BranchArray) Mytils.writeIn(".gitlet/BranchArray.txt");
        for (String i : array.getArray()) {
            if (i.equals(Mytils.getCurrentBranch())) {
                System.out.println("*" + Mytils.strip(i));
            } else {
                System.out.println(Mytils.strip(i));
            }
        }

        //prints out Staged Files Status
        System.out.println();
        System.out.println("=== Staged Files ===");
        StagingArea sArea = SAHelper.start();

        HashMap c = sArea.stagedFiles;
        Set<String> set = c.keySet();
        for (String s : set) {
            System.out.println(s);
        }

        //prints out Removed Files Status
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String s : sArea.removedFiles.keySet()) {
            System.out.println(s);
        }

        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");

        System.out.println();
        System.out.println("=== Untracked Files ===");
    }

    public static void globalLog() {
        HashSet<String> set = new HashSet<>();
        StagingArea sArea = SAHelper.start();
        BranchArray bArray = (BranchArray) Mytils.writeIn(".gitlet/BranchArray.txt");
        for (String branch : bArray.getArray()) {
            String commitPath = Mytils.getCurrentCommit(branch);
            Commit p = (Commit) Mytils.writeIn(commitPath);
            while (p != null) {
                if (set.contains(commitPath)) {
                    break;
                }
                set.add(commitPath);
                commitPath = p.getParent();
                p = (Commit) Mytils.writeIn(commitPath);
            }
        }

        // Add lost Commits to the set
        for (String lostCom : sArea.lostCommits) {
            set.add(lostCom);
        }

        for (String j : set) {
            Commit com = (Commit) Mytils.writeIn(j);
            System.out.println("===");
            System.out.println("Commit " + Mytils.strip(j));
            System.out.println(com.getTime());
            System.out.println(com.getMessage());
            System.out.println();
        }
    }

    public static void branch(String branchName) {
        if (new File(".gitlet/" + branchName + ".txt").exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        String commitPath = Mytils.getCurrentCommit();
        Branch newBranch = new Branch(branchName, commitPath);
        String branchPath = Mytils.serialize(newBranch, branchName);
        BranchArray bArray = (BranchArray) Mytils.writeIn(".gitlet/BranchArray.txt");
        bArray.add(branchPath);
        new File(".gitlet/BranchArray.txt").delete();
        Mytils.serialize(bArray, "BranchArray");
        return;
    }

    public static void rmBranch(String branchName) {
        if (!new File(".gitlet/" + branchName + ".txt").exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (Mytils.path(branchName).equals(Mytils.getCurrentBranch())) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        // Put commits to lostCommits
        StagingArea sArea = SAHelper.start();
        Branch branch = (Branch) Mytils.writeIn(Mytils.path(branchName));
        ArrayList<String> commits = branch.getCommits();
        for (int i = commits.size() - 1; i >= 0; i--) {
            sArea.lostCommits.add(commits.get(i));
        }
        SAHelper.finish(sArea);
        new File(Mytils.path(branchName)).delete();
        BranchArray bArray = (BranchArray) Mytils.writeIn(".gitlet/BranchArray.txt");
        bArray.remove(Mytils.path(branchName));
        new File(".gitlet/BranchArray.txt").delete();
        Mytils.serialize(bArray, "BranchArray");
    }

    public static void find(String message) {
        HashSet<String> returnSet = new HashSet<>();
        HashSet<String> set = new HashSet<>(); // Path of Commits we've checked
        BranchArray bArray = (BranchArray) Mytils.writeIn(".gitlet/BranchArray.txt");
        for (String branch : bArray.getArray()) {
            String currCommitPath = Mytils.getCurrentCommit(branch);
            Commit currCommit = (Commit) Mytils.writeIn(currCommitPath);
            while (currCommit != null) {
                if (set.contains(currCommitPath)) {
                    break;
                }
                set.add(currCommitPath);
                if (currCommit.getMessage().equals(message)) {
                    returnSet.add(Mytils.strip(currCommitPath));
                }
                currCommitPath = currCommit.getParent();
                currCommit = (Commit) Mytils.writeIn(currCommitPath);
            }
        }

        // Check lostCommits
        StagingArea sArea = SAHelper.start();
        for (String lostCommitPath : sArea.lostCommits) {
            if (!set.contains(lostCommitPath)) {
                set.add(lostCommitPath);
                Commit currCommit = (Commit) Mytils.writeIn(lostCommitPath);
                if (currCommit.getMessage().equals(message)) {
                    returnSet.add(Mytils.strip(lostCommitPath));
                }
            }
        }

        if (returnSet.isEmpty()) {
            System.out.println("Found no commit with that message.");
            return;
        }
        for (String j : returnSet) {
            System.out.println(j);
        }
    }

    //moving the first error checks
    public static boolean mergeFirstError(StagingArea sArea, String branchName) {


        if (!sArea.stagedFiles.isEmpty() || !sArea.removedFiles.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return false;
        }
        BranchArray branchArray = (BranchArray) Mytils.writeIn(Mytils.path("BranchArray"));
        if (!branchArray.getArray().contains(Mytils.path(branchName))) {
            System.out.println("A branch with that name does not exist.");
            return false;
        }
        return true;
    }

    public static boolean mergeSplitCommitPathError(Branch currBranch,
                                                    Branch givenBranch, String currentBranchPath,
                                                    String splitCommitPath) {
        if (splitCommitPath == null) {
            System.err.println("SPLIT FINDER DIDNT WORK!!");
            return false;
        }
        if (splitCommitPath.equals(givenBranch.getCurrent())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return false;
        }
        if (splitCommitPath.equals(currBranch.getCurrent())) {
            currBranch.changeCurrent(givenBranch.getCurrent());
            System.out.println("Current branch fast-forwarded.");
            Mytils.serialize(currBranch, Mytils.strip(currentBranchPath));
            return false;
        }
        return true;
    }

    public static boolean mergeFirstCases(HashMap<String, String> givenTracked,
                                          HashMap<String, String> splitTracked) {
        ArrayList<String> unTrackedFileNames = Mytils.getAllUntrackedFiles();
        for (String untrackedFile : unTrackedFileNames) {
            if (givenTracked.containsKey(untrackedFile)
                    &&
                    (!givenTracked.get(untrackedFile)
                            .equals
                                    (splitTracked.get(untrackedFile)))) {
                System.out.println("There is an untracked "
                        +
                        "file in the way; delete it or add it first.");
                return false;
            }
            if (splitTracked.containsKey(untrackedFile)
                    &&
                    (!givenTracked.containsKey(untrackedFile))) {
                System.out.println("There is an untracked file"
                        +
                        " in the way; delete it or add it first.");
                return false;
            }
        }
        return true;
    }

    public static String mergeGetSplitCommitPath(ArrayList<String> currCommits,
                                                 ArrayList<String> givenCommits) {
        for (int i = currCommits.size() - 1; i >= 0; i--) {
            if (givenCommits.contains(currCommits.get(i))) {
                return currCommits.get(i);
            }
        }
        return null;
    }

    public static void merge(String branchName) {
        StagingArea sArea = SAHelper.start();
        if (!mergeFirstError(sArea, branchName)) {
            return;
        }
        String currentBranchPath = Mytils.getCurrentBranch();
        //stores current branch
        Branch currBranch = (Branch) Mytils.writeIn(currentBranchPath);
        //other is the branch we want to merge
        Branch givenBranch = (Branch) Mytils.writeIn(Mytils.path(branchName));
        if (currBranch.equals(givenBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        //makes arrays with all the commit
        ArrayList<String> currCommits = currBranch.getCommits();
        ArrayList<String> givenCommits = givenBranch.getCommits();
        //finds split pt. commit
        String splitCommitPath = mergeGetSplitCommitPath(currCommits, givenCommits);

        //split error check
        if (!mergeSplitCommitPathError(currBranch, givenBranch, currentBranchPath,
                splitCommitPath)) {
            return;
        }
        Commit givenCurrentCommit = (Commit) Mytils.writeIn(givenBranch.getCurrent());
        Commit splitCommit = (Commit) Mytils.writeIn(splitCommitPath);
        HashMap<String, String> currTracked =
                (HashMap<String, String>) sArea.trackedFiles.clone();
        HashMap<String, String> givenTracked =
                (HashMap<String, String>) givenCurrentCommit.getDict().clone();
        HashMap<String, String> splitTracked = splitCommit.getDict();
        HashMap<String, String> newTracked = new HashMap<>();
        // test for error
        if (!mergeFirstCases(givenTracked, splitTracked)) {
            return;
        }
        // iterating thru split
        HashMap<String, Boolean> whatToStage = new HashMap<>();
        mergeIterThruWhatToStage(newTracked, splitTracked, whatToStage, givenTracked);

        // Put the rest of other in change to put in to curr branch. and add to what to stage
        for (String givenFile : givenTracked.keySet()) {
            newTracked.put(givenFile, givenTracked.get(givenFile));
            whatToStage.put(givenFile, true);
        }
        boolean conflicting = false;
        // iterating thru current
        conflicting = mergeItThruCurr(currTracked, newTracked,
                splitTracked, whatToStage, conflicting);
        // Auto Stage
        for (String fileName : newTracked.keySet()) {
            if (whatToStage.containsKey(fileName)) {
                if (whatToStage.get(fileName)) {
                    File blob = new File(newTracked.get(fileName));
                    File file = new File(System.getProperty("user.dir") + "/" + fileName);
                    file.delete();
                    try {
                        Files.copy(blob.toPath(), file.toPath());
                    } catch (IOException e) {
                        System.out.println("copy error2");
                        return;
                    }
                    //add(fileName);
                }
            }
        }
        for (String fileName : newTracked.keySet()) {
            if (whatToStage.get(fileName)) {
                SAHelper.addStagedFile(fileName);
            }
        }
        // Auto Commit
        if (!conflicting) {
            commit("Merged " + Mytils.strip(currentBranchPath) + " with " + branchName + ".");
        } else {
            System.out.println("Encountered a merge conflict.");
        }
        return;
    }

    public static void mergeIterThruWhatToStage(HashMap<String, String> newTracked,
                                                HashMap<String, String> splitTracked,
                                                HashMap<String, Boolean> whatToStage,
                                                HashMap<String, String> givenTracked) {
        for (String key : splitTracked.keySet()) {
            if (givenTracked.containsKey(key)) {
                if (!givenTracked.get(key).equals(splitTracked.get(key))) {
                    newTracked.put(key, givenTracked.get(key));
                }
                // will remove unchanged files
                givenTracked.remove(key);
                whatToStage.put(key, false);
            } else {
                // if given has removed the file
                newTracked.put(key, null);
                whatToStage.put(key, true);
            }
        }
    }

    public static boolean mergeItThruCurr(HashMap<String, String> currTracked,
                                       HashMap<String, String> newTracked,
                                       HashMap<String, String> splitTracked,
                                       HashMap<String, Boolean> whatToStage,
                                       boolean conflicting) {
        for (String key : currTracked.keySet()) {
            if (newTracked.containsKey(key)) {
                if (currTracked.get(key).equals(splitTracked.get(key))) {
                    // delete file from working directory
                    new File(System.getProperty("user.dir") + "/" + key).delete();
                    if (newTracked.get(key) != null) {
                        File blob = new File(newTracked.get(key));
                        File file = new File(System.getProperty("user.dir") + "/" + key);
                        try {
                            Files.copy(blob.toPath(), file.toPath());
                        } catch (IOException e) {
                            System.out.println("copy error1");
                        }
                        whatToStage.put(key, true);
                    } else {
                        whatToStage.put(key, false);
                        newTracked.remove(key);
                    }
                } else {
                    // String conflictFixer(String curr, String given, String filename) {
                    String resolvedPath = Mytils.conflictFixer(currTracked.get(key),
                            newTracked.get(key), key);
                    newTracked.put(key, resolvedPath);
                    whatToStage.put(key, false);
                    conflicting = true;
                }
            } else {

                newTracked.put(key, currTracked.get(key));
                whatToStage.put(key, true);
            }
        }
        return conflicting;
    }

    public static boolean mergeErrors(String branchName) {
        BranchArray branchArray = (BranchArray) Mytils.writeIn(Mytils.path("BranchArray"));
        if (!branchArray.getArray().contains(Mytils.path(branchName))) {
            System.out.println("A branch with that name does not exist.");
            return false;
        }
        String currentBranchPath = Mytils.getCurrentBranch();
        Branch currBranch = (Branch) Mytils.writeIn(currentBranchPath);
        Branch givenBranch = (Branch) Mytils.writeIn(Mytils.path(branchName));
        StagingArea sArea = SAHelper.start();
        ArrayList<String> currCommits = currBranch.getCommits();
        ArrayList<String> givenCommits = givenBranch.getCommits();
        String splitCommitPath = null;
        Commit givenCurrentCommit = (Commit) Mytils.writeIn(givenBranch.getCurrent());
        Commit splitCommit = (Commit) Mytils.writeIn(splitCommitPath);
        HashMap<String, String> currTracked =
                (HashMap<String, String>) sArea.trackedFiles.clone();
        HashMap<String, String> givenTracked =
                (HashMap<String, String>) givenCurrentCommit.getDict().clone();
        HashMap<String, String> splitTracked = splitCommit.getDict();
        HashMap<String, String> newTracked = new HashMap<>();
        ArrayList<String> unTrackedFileNames = Mytils.getAllUntrackedFiles();
        if (!sArea.stagedFiles.isEmpty() || !sArea.removedFiles.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return false;
        }
        if (currBranch.equals(givenBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return false;
        }

        if (splitCommitPath == null) {
            System.err.println("SPLIT FINDER DIDNT WORK!!");
            return false;
        }
        if (splitCommitPath.equals(givenBranch.getCurrent())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return false;
        }
        if (splitCommitPath.equals(currBranch.getCurrent())) {
            currBranch.changeCurrent(givenBranch.getCurrent());
            System.out.println("Current branch fast-forwarded.");
            Mytils.serialize(currBranch, Mytils.strip(currentBranchPath));
            return false;
        }

        for (String untrackedFile : unTrackedFileNames) {
            if (givenTracked.containsKey(untrackedFile)
                    &&
                    (!givenTracked.get(untrackedFile)
                            .equals
                                    (splitTracked.get(untrackedFile)))) {
                System.out.println("There is an untracked "
                        +
                        "file in the way; delete it or add it first.");
                return false;
            }
            if (splitTracked.containsKey(untrackedFile)
                    &&
                    (!givenTracked.containsKey(untrackedFile))) {
                System.out.println("There is an untracked file"
                        + " in the way; delete it or add it first.");
                return false;
            }
        }
        return true;
    }

    public static void reset(String commitID) {

        //checks if file doesn't exist
        if (!new File(Mytils.path(commitID)).exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Branch tempBranch = new Branch("temp", Mytils.path(commitID));
        Mytils.serialize(tempBranch, "temp");

        String currBranchPath = Mytils.getCurrentBranch();
        Branch currBranch = (Branch) Mytils.writeIn(currBranchPath);
        String startCommitPath = currBranch.getStart();
        ArrayList<String> commits = currBranch.getCommits();

        checkout3("temp");

        // Save commits with no branch pointers

        for (int i = commits.size() - 1; i >= 0; i--) {
            if (commits.get(i).equals(startCommitPath)) {
                break;
            }
            SAHelper.addLostCommits(commits.get(i));
        }

        currBranch.changeCurrent(Mytils.path(commitID));
        Mytils.serialize(currBranch, Mytils.strip(currBranchPath));
        Mytils.changeHeadBranch(currBranchPath);
        rmBranch("temp");
        SAHelper.newHead();
        return;
    }

}
