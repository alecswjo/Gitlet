package gitlet;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;




/**
 * Any addition to the Utils should be made here
 */
public class Mytils extends Utils {

    /* Returns the name of the serialized Commit */
    public static String serialize(Object c) {
        String hash = sha1(toByte(c)); // Create SHA-1 of Commit

        File outFile = new File(".gitlet/" + hash + ".txt");

        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(c);
            out.close();
        } catch (IOException excp) {
            System.out.println("Problem in serialize");
        }
        return ".gitlet/" + hash + ".txt";
    }

    public static String serialize(Object c, String name) {
        File outFile = new File(".gitlet/" + name + ".txt");
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(c);
            out.close();
        } catch (IOException excp) {
            System.out.println("Problem in serialize");
        }
        return ".gitlet/" + name + ".txt";
    }

    public static Object writeIn(String f) { //input is filename.txt
        if (f == null) {
            return null;
        }
        Object obj;
        File inFile = new File(f);
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
            obj = inp.readObject();
            inp.close();
            return obj;
        } catch (IOException | ClassNotFoundException excp) {
            obj = null;
            return obj;
        }
    }


    /* Converts the Object into a byte[] */
    public static byte[] toByte(Object obj) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(obj);
            objectStream.close();
            return stream.toByteArray();
        } catch (IOException excp) {
            //throw error("Internal error serializing commit.");
            System.out.println("Problem in toByte");
            return null;
        }

    }

    public static String getCurrentCommit() {
        Head head = (Head) writeIn(".gitlet/HEAD.txt");
        String branchPath = head.getBranch();
        Branch branch = (Branch) writeIn(branchPath);
        String commitPath = branch.getCurrent();
        return commitPath;

    }

    public static String getCurrentCommit(String branch) {
        Branch b = (Branch) writeIn(branch);
        String c = b.getCurrent();
        return c;

    }

    /* Returns the branchPath of branch pointed by HEAD */
    public static String getCurrentBranch() {
        Head head = (Head) writeIn(".gitlet/HEAD.txt");
        String branchPath = head.getBranch();
        return branchPath;
    }

    public static String strip(String c) {
        String[] hashArray = c.split("/");
        String[] hashArray2 = hashArray[1].split(".txt", 2);
        return hashArray2[0];
    }

    public static String path(String name) {
        return ".gitlet/" + name + ".txt";
    }

    public static String branchFinder(String commitID) {
        HashSet<String> set = new HashSet<>();
        BranchArray array = (BranchArray) Mytils.writeIn(".gitlet/BranchArray.txt");
        for (String i : array.getArray()) {
            String c = Mytils.getCurrentCommit(i);
            Commit p = (Commit) Mytils.writeIn(c);
            while (p != null) {
                if (set.contains(c)) {
                    break;
                }
                set.add(c);
                if (c.equals(Mytils.path(commitID))) {
                    return i;
                }
                c = p.getParent();
                p = (Commit) Mytils.writeIn(c);
            }

        }
        System.out.println("Commit could not be found in any branch.");
        return null;
    }

    /* Returns the path of a fixed conflict file */
    public static String conflictFixer(String currPath, String givenPath, String fileName) {

        byte[] head = ("<<<<<<< HEAD" + System.lineSeparator()).getBytes();
        byte[] currFile;
        if (currPath == null) {
            currFile = new byte[0];
        } else {
            currFile = Utils.readContents(new File(currPath));
        }
        byte[] givenFile;
        if (givenPath == null) {
            givenFile = new byte[0];
        } else {
            givenFile = Utils.readContents(new File(givenPath));
        }
        byte[] equals = ("=======" + System.lineSeparator()).getBytes();
        byte[] carrot = (">>>>>>>" + System.lineSeparator()).getBytes();
        byte[] one = new byte[head.length + currFile.length + equals.length
                + givenFile.length + carrot.length];
        System.arraycopy(head, 0, one, 0, head.length);
        System.arraycopy(currFile, 0, one, head.length, currFile.length);
        System.arraycopy(equals, 0, one, head.length
                + currFile.length, equals.length);
        System.arraycopy(givenFile, 0, one, head.length
                + currFile.length + equals.length, givenFile.length);
        System.arraycopy(carrot, 0, one, head.length
                + currFile.length + equals.length + givenFile.length, carrot.length);

        String path = System.getProperty("user.dir") + "/" + fileName;
        new File(path).delete();
        File file = new File(path);
        Utils.writeContents(file, one);
        return path;
    }

    public static byte[] concatByte(byte[] a, byte[] b) {
        byte[] one = new byte[a.length + b.length];
        System.arraycopy(a, 0, one, 0, a.length);
        System.arraycopy(b, 0, one, a.length, b.length);
        return one;
    }

    /* Returns an ArrayList of untracked fileNames */
    public static ArrayList<String> getAllUntrackedFiles() {

        File currDir = new File(System.getProperty("user.dir"));
        ArrayList<String> allWorkingFiles = new ArrayList<>(Utils.plainFilenamesIn(currDir));
        StagingArea sArea = SAHelper.start();

        // .gitlet directory is automatically not added


        //are you sure this is all untracked?
        for (String stagedFile : sArea.stagedFiles.keySet()) {
            allWorkingFiles.remove(stagedFile);
        }
        for (String trackedFile : sArea.trackedFiles.keySet()) {
            if (allWorkingFiles.contains(trackedFile)) {
                allWorkingFiles.remove(trackedFile);
            }
        }

        return allWorkingFiles;
    }

    public static void changeHeadBranch(String branchPath) {
        Head head = (Head) Mytils.writeIn(".gitlet/HEAD.txt");
        head.changeBranch(branchPath);
        Mytils.serialize(head, "HEAD");
        return;
    }

    public static String shortID(String shortenedID) {
        char[] shortChars = shortenedID.toCharArray();
        if (shortenedID.length() == 40) {
            return shortenedID;
        } else {
            BranchArray bArray = (BranchArray) Mytils.writeIn(".gitlet/BranchArray.txt");
            ArrayList<String> allBranches = bArray.getArray();
            for (String branch : allBranches) {
                Branch thisBranch = (Branch) Mytils.writeIn(branch);
                ArrayList<String> allCommits = thisBranch.getCommits();
                for (String commitPath : allCommits) {
                    String commitID = Mytils.strip(commitPath);
                    char[] commitChars = commitID.toCharArray();
                    boolean equals = true;
                    for (int i = 0; i < shortChars.length; i++) {
                        if (commitChars[i] != shortChars[i]) {
                            equals = false;
                            break;
                        }
                    }
                    if (equals) {
                        return commitID;
                    }

                }

            }
        }
        return shortenedID;
    }
}


