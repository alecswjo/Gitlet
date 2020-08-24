package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Date;

public class Commit implements Serializable {

    private final String message;
    private final String parent; // Path to parent
    // BlobMap fileName:filePath
    private HashMap<String, String> dictionary = new HashMap<>();
    private String timeStamp;

    /* Construct a Commit taking in a message, parent, and blobs */
    public Commit(String m) {
        message = m;
        parent = findParent(); // Figure out its own parent
        dictionary = findDict();
        timeStamp = findTime();
    }

    /* Constructor for initial commit */
    public Commit() {
        message = "initial commit";
        parent = null; // Figure out its own parent
        timeStamp = findTime();
    }


    /* Figures out the parent of this Commit */
    private static String findParent() {
        Head h = (Head) Mytils.writeIn(".gitlet/HEAD.txt");
        if (h == null) {
            return null;
        }
        String hb = h.getBranch();
        Branch b = (Branch) Mytils.writeIn(hb);
        return b.getCurrent();
    }

    private static String findTime() {
        Date dt = new Date();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(dt);
        return timestamp;
    }

    private static HashMap<String, String> findDict() {
        StagingArea sArea = SAHelper.start();
        sArea.trackedFiles.putAll(SAHelper.peekStagedFiles());
        for (String removedFileName : SAHelper.peekRemovedFiles().keySet()) {
            sArea.trackedFiles.remove(removedFileName);
        }
        HashMap<String, String> dict = (HashMap<String, String>) sArea.trackedFiles.clone();

        SAHelper.finish(sArea);
        SAHelper.refresh();
        return dict;
    }

    /* Returns the parent of the Commit */
    public String getParent() {
        return parent;
    }

    /* Returns the message of the Commit */
    public String getMessage() {
        return message;
    }

    public HashMap<String, String> getDict() {
        return dictionary;
    }

    public String getTime() {
        return timeStamp;
    }
}
