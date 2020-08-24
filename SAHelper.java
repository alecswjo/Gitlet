package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;

public class SAHelper {

    public static StagingArea start() {
        StagingArea sArea = (StagingArea) Mytils.writeIn(".gitlet/StagingArea.txt");
        return sArea;
    }

    public static void finish(StagingArea sArea) {
        Mytils.serialize(sArea, "StagingArea");
        return;
    }

    /* Get Methods */
    public static HashMap<String, String> peekStagedFiles() {
        StagingArea sArea = start();
        return sArea.stagedFiles;
    }

    public static HashMap<String, String> peekTrackedFiles() {
        StagingArea sArea = start();
        return sArea.trackedFiles;
    }

    public static HashMap<String, String> peekRemovedFiles() {
        StagingArea sArea = start();
        return sArea.removedFiles;
    }

    public static HashMap<String, String> peekUntrackedFiles() {
        StagingArea sArea = start();
        return sArea.untrackedFiles;
    }

    public static HashSet<String> peekLostCommits() {
        StagingArea sArea = start();
        return sArea.lostCommits;
    }


    public static void addStagedFile(String fileName) {
        StagingArea sArea = start();

        File addFile = new File(System.getProperty("user.dir") + "/" + fileName);
        String filePath = Mytils.path
                (Utils.sha1(Mytils.concatByte(Utils.readContents(addFile),
                        Mytils.toByte(fileName))));
        File newFile = new File(filePath); //makes an empty file with the SHA-1 in its Path
        if (sArea.untrackedFiles.containsKey(fileName)) {
            sArea.stagedFiles.put(fileName, filePath);
        } else if (sArea.removedFiles.containsKey(fileName)) {
            sArea.removedFiles.remove(fileName);
            finish(sArea);
            addStagedFile(fileName); // : Recursive
        } else if (sArea.trackedFiles.containsKey(fileName)) {
            if (!sArea.trackedFiles.get(fileName).equals(filePath)) {
                sArea.stagedFiles.put(fileName, filePath);
            }
        } else {
            sArea.stagedFiles.put(fileName, filePath);
        }

        try { // : When do we have to copy and when do we not?
            Files.copy(addFile.toPath(), newFile.toPath()); //creates blob
        } catch (FileAlreadyExistsException e) {
            finish(sArea);
            return;
        } catch (IOException e) {
            System.out.println(e);
            System.out.println("copy error");
        }
        finish(sArea);
    }

    /* Returns true if removed file, returns false if file is not staged */
    public static boolean removeStagedFile(String fileName) {
        StagingArea sArea = start();
        boolean worked = false;

        String filePath = null;
        if (sArea.stagedFiles.containsKey(fileName)) {
            filePath = sArea.stagedFiles.get(fileName);
            sArea.stagedFiles.remove(fileName);
            worked = true;
        }
        if (sArea.trackedFiles.containsKey(fileName)) {
            if (filePath != null) {
                sArea.removedFiles.put(fileName, filePath);
            } else {
                sArea.removedFiles.put(fileName, sArea.trackedFiles.get(fileName));
                // : put three lines below into else{}
                File toDeleteFile = new File(System.getProperty("user.dir") + "/" + fileName);
                toDeleteFile.delete();
                worked = true;
            }
        }
        finish(sArea);
        return worked; // : Throw Errors outside if false
    }

    public static void refresh() {
        StagingArea sArea = start();
        sArea.untrackedFiles.putAll(sArea.removedFiles);
        for (String stagedFileName : SAHelper.peekStagedFiles().keySet()) {
            sArea.untrackedFiles.remove(stagedFileName);
        }
        sArea.removedFiles.clear();
        sArea.stagedFiles.clear();
        finish(sArea);
        return;
    }

    public static void newHead() {
        StagingArea sArea = start();
        String commitPath = Mytils.getCurrentCommit();
        Commit commit = (Commit) Mytils.writeIn(commitPath);

        sArea.stagedFiles.clear();
        sArea.removedFiles.clear();
        sArea.untrackedFiles.clear();
        sArea.trackedFiles = (HashMap<String, String>) commit.getDict().clone();
        finish(sArea);
        return;

    }

    public static void addLostCommits(String commitPath) {
        StagingArea sArea = start();
        sArea.lostCommits.add(commitPath);
        finish(sArea);
        return;
    }

    public static void removedFromTrackedFileSB(String fileName) {
        StagingArea sArea = start();
        sArea.trackedFiles.remove(fileName);
        finish(sArea);
    }
}
