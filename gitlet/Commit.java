package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * The commit data structure for Gitlet.
 * @author Mariel Aquino
 */

@SuppressWarnings("unchecked")
public class Commit implements Serializable {

    /** Blobs associated with this node.
     * A mapping of filenames to their SHA1. */
    private TreeMap<String, String> directoryTree;
    /** Tracking the branches. */
    private HashMap<String, String> branchesTree;
    /** The assigned commit message. */
    private String commitMessage;
    /** The SHA1 ID. */
    private String id;
    /** The timestamp. */
    private String timeStamp;
    /** The parent node. */
    private String parentHash;
    /** The second parent node for merging. */
    private String secParentHash;
    /** Something to find split point. */
    private long splitTime;


    /** Constructed empty for initializing. */
    public Commit() {
    }

    /**
     * Commit object constructor. A commit is a tree of
     * blobs, pointing to parents by referencing their sha1
     * hash codes.
     * @param message of commit
     * @param parent is null
     * @param isHeadBool is unused
     */
    public Commit(String message, Commit parent, boolean isHeadBool) {
        commitMessage = message;
        parentHash = parent.getId();
        splitTime = System.currentTimeMillis();
        timeStamp = new SimpleDateFormat("EEE MMM d HH:mm:ss "
                + "yyyy Z").format(new java.util.Date());
        directoryTree = (TreeMap) setDirectoryTree(parent);
        id = setId();
        secParentHash = null;
    }

    /**
     * Initial commit constructor.
     * @param message of commit
     * @param parent is null
     */
    public Commit(String message, Commit parent) {
        commitMessage = message;
        parentHash = "hey ma";
        timeStamp = "Wed Dec 31 16:00:00 1969 -0800";
        directoryTree = new TreeMap<String, String>();
        secParentHash = null;
        id = setId();
    }

    /**
     * Set the associated files to the commit.
     * @param parent commit object
     * @return a clone of the parent commit's directory tree
     */
    public Object setDirectoryTree(Commit parent) {
        return parent.getDirectoryTree().clone();
    }

    /**
     * Used to set the commit id.
     * @return String of commit ID for constructor.
     */
    public String setId() {
        String shaID = timeStamp + commitMessage;
        if (parentHash != null) {
            shaID += parentHash;
        }
        for (String value : directoryTree.values()) {
            shaID += value;
        }
        id = Utils.sha1(shaID);
        return id;

    }

    /**
     * Find the head commit's sha as a string.
     * @return string SHA1 of head commit
     */
    public static String findHeadHelper() {
        File currHead = new File(".gitlet/currHead.txt");
        String temp = Utils.readContentsAsString(currHead);
        String sha1 = Utils.readContentsAsString(new
                File(".gitlet/.branches/" + temp));

        return sha1;
    }

    /**
     * Find the SHA1 of a certain blob, given its filename
     * and the commit that holds the file.
     * @param commitSHA1 of the commit which holds the file
     * @param fileName of the blob you need the sha of
     * @return
     */
    public static String findFileSha(String commitSHA1, String fileName) {
        File f = new File(".gitlet/.commits/" + commitSHA1);
        Commit temp = Utils.readObject(f, Commit.class);
        TreeMap<String, String> tempHash = temp.directoryTree;
        return tempHash.get(fileName);
    }

    /** Return the commit tree object with a specific sha.
     * @param sha1 of the commit you need
     * @return a directory tree
     */
    public static TreeMap findCommitTree(String sha1) {
        File f = new File(".gitlet/.commits/" + sha1);
        Commit temp = Utils.readObject(f, Commit.class);
        TreeMap<String, String> tempHash = temp.directoryTree;
        return tempHash;
    }

    /** Helper method to find the commit which corresponds to a certain SHA1.
     * @param sha1 for the sha
     * @return temp commit object*/
    public static Commit findCommit(String sha1) {
        File f = new File(".gitlet/.commits/" + sha1);
        Commit temp = Utils.readObject(f, Commit.class);
        return temp;
    }

    /** Helper method to write a commit object back. It will make
     * the file and serialize the commit object for you.
     * @param sha1 to write into the file
     * @param temp to serialize into the file
     */
    public static void writeCommit(String sha1, Commit temp) {
        File f = new File(".gitlet/.commits/" + sha1);
        Utils.writeObject(f, temp);
        return;
    }


    /** GETTER METHODS */

    /** Returns the HashMap of blob references for this commit.
     * @return HashMap for blob references*/
    public TreeMap<String, String> getDirectoryTree() {
        return directoryTree;
    }

    /** Returns the commit message for this commit.
     * @return String for commit message */
    public String getCommitMessage() {
        return commitMessage;
    }

    /** Returns the id for this commit.
     * @return id for committer's id */
    public String getId() {
        return id;
    }

    /** Returns the time stamp for this commit.
     * @return Long for timestamp */
    public String getTimeStamp() {
        return timeStamp;
    }

    /** Returns the parent node's SHA1.
     * @return String for parent node SHA1 */
    public String getParentHash() {
        return parentHash;
    }


    /** Prints commit information. */
    public void printCommit() {
        System.out.println("===");
        System.out.println("commit " + id);
        System.out.println("Date: " + timeStamp);
        System.out.println(commitMessage);
        System.out.println();
    }

    /**
     * Getter method.
     * @return split time
     */
    public long getSplitTime() {
        return splitTime;
    }

}
