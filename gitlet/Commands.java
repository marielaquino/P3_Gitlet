package gitlet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


/**
 * A class for the Gitlet commands.
 * Includes: init, add, commit, rm, log, find,
 * checkout, rm-branch, branch, reset, and merge.
 * <p>
 * Dangerous: rm, checkout, reset, merge
 * <p>
 * EXTRA CREDIT REMOTE COMMANDS: push, pull, ??
 *
 * @author Mariel Aquino
 */
@SuppressWarnings("unchecked")
public class Commands {

    /** SIMPLIFYING STRINGS */

    /**
     * Name for the gitlet directory.
     */
    private static final String GITLET_DIR = ".gitlet/";
    /**
     * Name for the staging directory.
     */
    private static final String STAGING_DIR = ".staging/";
    /**
     * Name for the directory.
     */
    private static final String REMOVE_DIR = ".remove/";
    /**
     * Name for the commit directory.
     */
    private static final String COMMIT_DIR = ".commits/";
    /**
     * Name for the branches directory.
     */
    private static final String BRANCHES_DIR = ".branches/";
    /**
     * Name for the blobs directory.
     */
    private static final String BLOBS_DIR = ".blobs/";

    /** HASH MAP OBJECTS FOR TRACKING STAGING, REMOVING, AND BRANCHES. */

    /**
     * Hash of staged files.
     */
    private static HashMap<String, String> toStage = new HashMap<>();
    /**
     * Hash of files to be removed.
     */
    private static HashMap<String, String> toRemove = new HashMap<>();
    /**
     * Hash of branches to their commit heads.
     */
    private static HashMap<String, String> branches = new HashMap<>();

    /** ACCESSING DIRECTORIES FOR TRACKING OBJECTS. */

    /**
     * Directory for branches.
     */
    private static File branchFile = new File(
            GITLET_DIR + BRANCHES_DIR + "branchobj");
    /**
     * File for staging.
     */
    private static File stagingFile = new File(
            GITLET_DIR + STAGING_DIR + "stagingobj");
    /**
     * File for removing.
     */
    private static File removeFile = new File(
            GITLET_DIR + REMOVE_DIR + "removeobj");
    /**
     * File that holds current branch name.
     */
    private static File currHead = new File(GITLET_DIR + "currHead.txt");
    /**
     * File that holds the master branch with its head commit sha.
     */
    private static File master = new File(".gitlet/.branches/master");
    /**
     * Access the gitlet working directory.
     */
    private static File dir = new File(GITLET_DIR);
    /**
     * Access staging directory.
     */
    private static File stag = new File(GITLET_DIR + STAGING_DIR);
    /**
     * Access removing directory.
     */
    private static File remov = new File(GITLET_DIR + REMOVE_DIR);
    /**
     * Access commits directory.
     */
    private static File comm = new File(GITLET_DIR + COMMIT_DIR);
    /**
     * Access blobs directory.
     */
    private static File blobs = new File(GITLET_DIR + BLOBS_DIR);
    /**
     * Access branch directory.
     */
    private static File branch = new File(GITLET_DIR + BRANCHES_DIR);
    /**
     * Access initial commit.
     */
    private static Commit initComm = new Commit("initial commit", null);


    /**
     * Creates a new .gitlet/ directory if one does not
     * currently exist.
     * @throws IOException
     */
    public static void initCommand() throws IOException {
        if (!dir.exists()) {
            dir.mkdir();
            stag.mkdir();
            remov.mkdir();
            comm.mkdir();
            branch.mkdir();
            blobs.mkdir();
            currHead.createNewFile();
            branches.put("master", initComm.getId());
            Utils.writeObject(new File(".gitlet/.commits/"
                    + initComm.getId()), initComm);
            editFile(currHead, "master");
            editFile(master, initComm.getId());
            Utils.writeObject(stagingFile, toStage);
            Utils.writeObject(removeFile, toRemove);
            Utils.writeObject(branchFile, branches);
        } else {
            throw new GitletException("A gitlet version control"
                    + " system already exists in the current directory.");
        }
    }

    /**
     * Returns the name of the current head branch as a string.
     * @return temp string of current head branch name
     */
    public static String getBranch() {
        String temp = Utils.readContentsAsString(currHead);
        return temp;
    }

    /**
     * Returns string of the current head commit, given the branch name.
     * @param branchName is a branch name
     * @return sha1 string of the current head commit
     */
    public static String getCurrHeadCommSHA(String branchName) {
        String sha1 = Utils.readContentsAsString(new File(fileString(branchName)));
        return sha1;
    }

    /** Please stop complaining.
     * @param branchName for the branch
     * @return fileString to be returned
     */
    public static String fileString(String branchName) {
        String fileString = ".gitlet/.branches/" + branchName;
        return fileString;
    }

    /**
     * Return a copy of the text file which holds the name
     * of the head commit. Used to update the head commit
     * in the commit function.
     * @return a file
     */
    public static File getBranchFile() {
        File thisBranch = new File(GITLET_DIR + BRANCHES_DIR + getBranch());
        return thisBranch;
    }

    /**
     * Gets the specific branch file.
     * @param branchName string
     * @return thisBranch string
     */
    public static File getSpecificBranchFile(String branchName) {
        File thisBranch = new File(GITLET_DIR + BRANCHES_DIR + branchName);
        return thisBranch;
    }

    /**
     * Edits a text file to hold certain text.
     * @param file   that you need to edit
     * @param string that you want to place in the file
     * @throws IOException
     */
    public static void editFile(File file, String string) throws IOException {
        file.createNewFile();
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(string);
        bw.flush();
        bw.close();
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        br.close();
    }

    /**
     * Adding a file to be committed in the next commit.
     *
     * @param fileName for the file we're modifying
     */
    public static void addCommand(String fileName) throws IOException {
        File f = new File(fileName);
        if (!f.exists()) {
            throw new GitletException("File does not exist.");
        }
        HashMap<String, String> stageTemp = Utils.readObject(
                stagingFile, HashMap.class);
        HashMap<String, String> removeTemp = Utils.readObject(
                removeFile, HashMap.class);
        String blobSha = Utils.sha1(Utils.readContentsAsString(f) + fileName);
        String blobCont = Utils.readContentsAsString(f);
        if (removeTemp.containsKey(fileName)) {
            removeTemp.remove(fileName);
            Utils.writeObject(removeFile, removeTemp);
        }
        if (stageTemp.containsKey(fileName)) {
            if (blobSha.equals(stageTemp.get(fileName))) {
                throw new GitletException("Already exists");
            }
        }
        if (blobSha.equals(Commit.findFileSha(
                Commit.findHeadHelper(), fileName))) {
            if (stageTemp.containsKey(fileName)) {
                stageTemp.remove(fileName);
                Utils.writeObject(stagingFile, stageTemp);
            }
            System.exit(0);
        }
        stageTemp.put(fileName, blobSha);
        File stagCopy = new File(GITLET_DIR + STAGING_DIR
                + stageTemp.get(fileName));
        File blobsCopy = new File(
                GITLET_DIR + BLOBS_DIR + stageTemp.get(fileName));
        stagCopy.createNewFile();
        blobsCopy.createNewFile();
        Utils.writeObject(stagCopy, f);
        Utils.writeContents(stagCopy, blobCont);
        Utils.writeObject(blobsCopy, f);
        Utils.writeContents(blobsCopy, blobCont);
        Utils.writeObject(stagingFile, stageTemp);
    }

    /**
     * Commit commmand.
     * @param args is the message of the commit
     * @throws IOException
     */
    public static void commitCommand(String args) throws IOException {
        HashMap<String, String> stageTemp = Utils.readObject(
                stagingFile, HashMap.class);
        HashMap<String, String> removTemp = Utils.readObject(
                removeFile, HashMap.class);
        if (args.length() == 0) {
            throw new GitletException("Please enter a commit message.");
        }

        if (stageTemp.size() == 0 && removTemp.size() == 0) {
            throw new GitletException("No changes added to the commit.");
        }
        Commit parentComm = Commit.findCommit(Commit.findHeadHelper());
        Commit nextComm = new Commit(args, parentComm, false);
        nextComm.getDirectoryTree().putAll(stageTemp);
        for (String key : removTemp.keySet()) {
            nextComm.getDirectoryTree().remove(key);
        }
        editFile(getBranchFile(), nextComm.getId());
        HashMap<String, String> branchTemp = Utils.readObject(
                branchFile, HashMap.class);
        branchTemp.put(getBranch(), nextComm.getId());
        Utils.writeObject(branchFile, branchTemp);

        File nextCommF = new File(GITLET_DIR + COMMIT_DIR + nextComm.getId());
        nextCommF.createNewFile();
        Utils.writeObject(nextCommF, nextComm);

        List<String> shaList = Utils.plainFilenamesIn(GITLET_DIR + STAGING_DIR);
        for (String obj : shaList) {
            Utils.restrictedDelete(obj);
        }
        stageTemp.clear();
        removTemp.clear();

        Utils.writeObject(stagingFile, stageTemp);
        Utils.writeObject(removeFile, removTemp);

    }

    /**
     * Remove command.
     * @param fileName that you're removing
     */

    public static void rmCommand(String fileName) {
        HashMap<String, String> stageTemp = Utils.readObject(
                stagingFile, HashMap.class);
        HashMap<String, String> removTemp = Utils.readObject(
                removeFile, HashMap.class);
        Commit holdComm = Commit.findCommit(Commit.findHeadHelper());
        String sha1 = stageTemp.get(fileName);
        removTemp.put(fileName, sha1);
        if (!stageTemp.containsKey(fileName)
                && !holdComm.getDirectoryTree().containsKey(fileName)) {
            throw new GitletException("No reason to remove the file.");
        }
        if (stageTemp.containsKey(fileName)) {
            stageTemp.remove(fileName);
        }
        if (holdComm.getDirectoryTree().containsKey(fileName)) {
            Utils.restrictedDelete(fileName);
        }
        Utils.writeObject(stagingFile, stageTemp);
        Utils.writeObject(removeFile, removTemp);
        Commit.writeCommit(holdComm.getId(), holdComm);

    }

    /**
     * Log command.
     */
    public static void logCommand() {
        Commit currComm = Commit.findCommit(Commit.findHeadHelper());
        while (!currComm.getCommitMessage().equals("initial commit")) {
            currComm.printCommit();
            currComm = Commit.findCommit(currComm.getParentHash());
            if (currComm.getCommitMessage().equals("initial commit")) {
                currComm.printCommit();
                break;
            }
        }
    }


    /**
     * Global log command. Takes no args.
     */
    public static void globalLogCommand() {
        List<String> commitNames = Utils.plainFilenamesIn(".gitlet/.commits/");
        for (String name : commitNames) {
            Commit temp = Utils.readObject(new File(
                    GITLET_DIR + COMMIT_DIR + name), Commit.class);
            temp.printCommit();
        }
    }

    /**
     * Find command.
     * @param args of the commit message you'd like to find commits for
     */
    public static void findCommand(String args) {
        List<String> commitNames = Utils.plainFilenamesIn(
                GITLET_DIR + COMMIT_DIR);
        boolean namesFound = false;
        for (String name : commitNames) {
            Commit temp = Utils.readObject(new File(
                    GITLET_DIR + COMMIT_DIR + name), Commit.class);
            if (temp.getCommitMessage().equals(args)) {
                System.out.println(temp.getId());
                namesFound = true;
            }
        }
        if (!namesFound) {
            throw new GitletException("Found no commit with that message.");
        }
    }

    /**
     * Status command.
     */
    public static void statusCommand() {
        List<String> branchList = Utils.plainFilenamesIn(
                GITLET_DIR + BRANCHES_DIR);
        System.out.println("=== Branches ===");
        for (String brancho : branchList) {
            if (brancho.equals(getBranch())) {
                System.out.println("*" + getBranch());
            } else if (brancho.equals("branchobj")) {
                continue;
            } else {
                System.out.println(brancho);
            }
        }
        System.out.println();
        TreeMap<String, String> stagedList = new TreeMap<String, String>();
        HashMap<String, String> stageTemp = Utils.readObject(
                stagingFile, HashMap.class);
        stagedList.putAll(stageTemp);
        System.out.println("=== Staged Files ===");
        for (String key : stagedList.keySet()) {
            System.out.println(key);
        }
        System.out.println();
        TreeMap<String, String> removedList = new TreeMap<String, String>();
        HashMap<String, String> removeTemp = Utils.readObject(
                removeFile, HashMap.class);
        TreeMap<String, String> holdTree = Commit.findCommitTree(
                getCurrHeadCommSHA(getBranch()));
        removedList.putAll(removeTemp);
        System.out.println("=== Removed Files ===");
        for (String key : removedList.keySet()) {
            if (holdTree.containsKey(key)) {
                System.out.println(key);
            }
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        List<String> modNotStaged = modNotStagedFiles();
        for (String mod : modNotStaged) {
            System.out.println(mod);
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        List<String> untracked = untrackedFiles();
        for (String untrack : untracked) {
            System.out.println(untrack);
        }
        System.out.println();

    }

    /**
     * First checkout function.
     * @param fileName of the file we want to checkout.
     * @throws IOException
     */
    public static void checkoutFirstHelper(String fileName) throws IOException {
        String headCommSHA = getCurrHeadCommSHA(getBranch());
        if (!Commit.findCommitTree(headCommSHA).containsKey(fileName)) {
            throw new GitletException("File does not exist in that commit.");
        }
        String blobSha = Commit.findFileSha(headCommSHA, fileName);
        File blobFile = new File(GITLET_DIR + BLOBS_DIR + blobSha);
        String blobCont = Utils.readContentsAsString(blobFile);
        File newBlob = new File("./" + fileName);
        Utils.writeObject(newBlob, blobFile);
        Utils.writeContents(newBlob, blobCont);


    }

    /**
     * Second checkout function.
     * @param fileName of the filename we want to checkout
     * @param commitId that contains the version of the file we want to checkout
     * @param whatThe for some random test
     */
    public static void checkoutSecondHelper(String fileName,
                                            String commitId, String whatThe) {
        List<String> allCommIds = Utils.plainFilenamesIn(comm);
        if (!allCommIds.contains(commitId)) {
            throw new GitletException("No commit with that id exists.");
        }
        TreeMap<String, String> holdTree = Commit.findCommitTree(commitId);
        if (whatThe.equals("++")) {
            throw new GitletException("Incorrect operands.");
        }
        if (!holdTree.containsKey(fileName)) {
            throw new GitletException("File does not exist in that commit.");
        }
        String blobSha = Commit.findFileSha(commitId, fileName);
        File blobFile = new File(GITLET_DIR + BLOBS_DIR + blobSha);
        String blobCont = Utils.readContentsAsString(blobFile);
        File newBlob = new File("./" + fileName);
        Utils.writeObject(newBlob, blobFile);
        Utils.writeContents(newBlob, blobCont);
    }

    /**
     * Third checkout function.
     * @param branchName of the branch that we want to check out
     * @throws IOException
     */
    public static void checkoutThirdHelper(String branchName)
            throws IOException {
        List<String> allBranch = Utils.plainFilenamesIn(branch);
        if (!allBranch.contains(branchName)) {
            throw new GitletException("No such branch exists.");
        }
        if (branchName.equals(getBranch())) {
            throw new GitletException("No need to "
                    + "checkout the current branch.");
        }
        untrackedFileInWay(getCurrHeadCommSHA(branchName));
        TreeMap<String, String> tempCommHash = Commit.findCommitTree(
                getCurrHeadCommSHA(branchName));
        Commit headComm = Commit.findCommit(getCurrHeadCommSHA(getBranch()));

        TreeMap<String, String> headCommTree = Commit.findCommitTree(
                Commit.findHeadHelper());

        Set<Map.Entry<String, String>> branchSet = tempCommHash.entrySet();
        Set<Map.Entry<String, String>> headCommSet = headCommTree.entrySet();
        Set<Map.Entry<String, String>> unionSet = new HashSet<>();
        unionSet.addAll(branchSet);
        unionSet.addAll(headCommSet);
        for (Map.Entry<String, String> entry : unionSet) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (headCommTree.containsKey(key)) {
                if (!tempCommHash.containsKey(key)) {
                    Utils.restrictedDelete(key);
                }
            }
            if (tempCommHash.containsKey((key))) {
                File blobFile = new File(GITLET_DIR
                        + BLOBS_DIR + tempCommHash.get(key));
                String blobCont = Utils.readContentsAsString(blobFile);
                File newBlob = new File("./" + entry.getKey());
                Utils.writeObject(newBlob, blobFile);
                Utils.writeContents(newBlob, blobCont);
            }

        }
        Commit branchComm = Commit.findCommit(getCurrHeadCommSHA(branchName));
        String branchCommID = getCurrHeadCommSHA(branchName);
        editFile(getSpecificBranchFile(branchName), branchCommID);
        editFile(currHead, branchName);

        HashMap<String, String> stageTemp = Utils.readObject(
                stagingFile, HashMap.class);
        stageTemp.clear();
        Utils.writeObject(stagingFile, stageTemp);

    }

    /**
     * Branch command.
     *
     * @param args of the branch we want to create.
     * @throws IOException
     */
    public static void branchCommand(String args) throws IOException {
        HashMap<String, String> branchTemp = Utils.readObject(
                branchFile, HashMap.class);
        if (branchTemp.containsKey(args)) {
            throw new GitletException("A branch "
                    + "with that name already exists.");
        }
        String headCommSha = Commit.findCommit(Commit.findHeadHelper()).getId();
        branchTemp.put(args, headCommSha);
        File temp = new File(GITLET_DIR + BRANCHES_DIR + args);
        temp.createNewFile();
        editFile(temp, headCommSha);
        Utils.writeObject(branchFile, branchTemp);
    }

    /**
     * Rm command.
     * @param args of the branch we want to remove
     */
    public static void rmBranchCommand(String args) {
        List<String> branchList = Utils.plainFilenamesIn(
                GITLET_DIR + BRANCHES_DIR);
        if (!branchList.contains(args)) {
            throw new GitletException("A branch with that "
                    + "name does not exist.");
        }
        if (args.equals(getBranch())) {
            throw new GitletException("Cannot remove the current branch.");
        }
        HashMap<String, String> branchTemp = Utils.readObject(
                branchFile, HashMap.class);
        branchTemp.remove(args);
        File temp = new File(GITLET_DIR + BRANCHES_DIR + args);
        temp.delete();
        Utils.writeObject(branchFile, branchTemp);
    }

    /**
     * Reset command.
     * @param args sha of the commit we want to reset to
     * @throws IOException
     */

    public static void resetCommand(String args) throws IOException {
        List<String> allCommits = Utils.plainFilenamesIn(comm);
        if (!allCommits.contains(args)) {
            throw new GitletException("No commit with that id exists.");
        }
        List<String> workinDir = Utils.plainFilenamesIn(new File("."));
        Set<String> headIDSet = Commit.findCommitTree(
                getCurrHeadCommSHA(getBranch())).keySet();
        Set<String> commdIDSet = Commit.findCommitTree(args).keySet();
        for (String key : commdIDSet) {
            if (!headIDSet.contains(key) && workinDir.contains(key)) {
                throw new GitletException("There is an "
                        + "untracked file in the way; delete it or add it first.");

            }
        }
        TreeMap<String, String> tempCommHash = Commit.findCommitTree(args);
        for (Map.Entry<String, String> entry : tempCommHash.entrySet()) {
            File blobFile = new File(GITLET_DIR + BLOBS_DIR + entry.getValue());
            String blobCont = Utils.readContentsAsString(blobFile);
            File newBlob = new File("./" + entry.getKey());
            Utils.writeObject(newBlob, blobFile);
            Utils.writeContents(newBlob, blobCont);
        }
        HashMap<String, String> stageTemp = Utils.readObject(
                stagingFile, HashMap.class);
        HashMap<String, String> removTemp = Utils.readObject(
                removeFile, HashMap.class);
        for (String key : stageTemp.keySet()) {
            if (!commdIDSet.contains(key)) {
                removTemp.remove(key);
                File temp = new File(GITLET_DIR
                        + REMOVE_DIR + stageTemp.get(key));
                temp.delete();

            }
        }
        stageTemp.clear();
        editFile(getBranchFile(), args);
        Utils.writeObject(removeFile, removTemp);
        Utils.writeObject(stagingFile, stageTemp);

    }

    /**
     * Return staging map.
     * @return stagetemp
     */
    public static HashMap<String, String> returnStage() {
        HashMap<String, String> stageTemp = Utils.readObject(
                stagingFile, HashMap.class);
        return stageTemp;

    }

    /**
     * Return remove map.
     * @return removtemp
     */
    public static HashMap<String, String> returnRemov() {
        HashMap<String, String> removTemp = Utils.readObject(
                removeFile, HashMap.class);
        return removTemp;

    }

    /**
     * Throw exception.
     * @param stageTemp map
     * @param removTemp map
     */
    public static void throwUncommitExc(HashMap<String, String> stageTemp,
                                        HashMap<String, String> removTemp) {
        if (!(stageTemp.size() == 0) || !(removTemp.size() == 0)) {
            throw new GitletException("You have uncommited changes.");
        }
    }

    /**
     * Merge with itself.
     * @param branchName name
     */
    public static void mergeWithItself(String branchName) {
        if (branchName.equals(getBranch())) {
            throw new GitletException("Cannot merge a branch with itself.");
        }
    }

    /**
     * Special cases error.
     * @param splitHeadSHA commit
     * @param branchHeadSHA commit
     * @param currHeadSHA commit
     * @throws IOException
     */
    public static void specialCasesErr(String splitHeadSHA,
                                       String branchHeadSHA,
                                       String currHeadSHA) throws IOException {
        if (splitHeadSHA.equals(branchHeadSHA)) {
            throw new GitletException("Given branch is an ancestor"
                    + " of the current branch.");
        } if (splitHeadSHA.equals(currHeadSHA)) {
            editFile(currHead, branchHeadSHA);
            throw new GitletException("Current branch fast-forwarded.");
        }
    }

    /**
     * Shorten the method.
     * @param splitHeadSHA commit
     * @param currHeadSHA commit
     * @param branchHeadSHA commit
     * @param stageTemp hashmap
     * @param removTemp hashmap
     */
    public static void mapHelper(String splitHeadSHA,
                                 String currHeadSHA,
                                 String branchHeadSHA,
                                 HashMap<String, String> stageTemp,
                                 HashMap<String, String> removTemp) {
        TreeMap<String, String> branchMap = Commit.findCommitTree(branchHeadSHA);
        TreeMap<String, String> splitMap = Commit.findCommitTree(splitHeadSHA);
        TreeMap<String, String> currMap = Commit.findCommitTree(currHeadSHA);
        for (String key : branchMap.keySet()) {
            if (!splitMap.containsKey(key)) {
                checkoutSecondHelper(key, branchHeadSHA, "--");
                stageTemp.put(key, branchMap.get(key));
            } else if (!splitMap.get(key).equals(branchMap.get(key))) {
                checkoutSecondHelper(key, branchHeadSHA, "--");
                stageTemp.put(key, branchMap.get(key));
            }
        }
        for (String key : splitMap.keySet()) {
            if (currMap.containsKey(key)) {
                if (splitMap.get(key).equals(currMap.get(key))
                        && !branchMap.keySet().contains(key)) {
                    removTemp.put(key, currMap.get(key));
                    Utils.restrictedDelete(key);
                }
            }
        }
        for (String key : branchMap.keySet()) {
            if (currMap.containsKey(key)) {
                if (!currMap.get(key).equals(branchMap.get(key))) {
                    System.out.println("Encountered a merge conflict.");
                    File conflictCase = new File(key);
                    String currCont = Utils.readContentsAsString(
                            new File(GITLET_DIR + BLOBS_DIR + currMap.get(key)));
                    String brCont = Utils.readContentsAsString(
                            new File(GITLET_DIR + BLOBS_DIR + branchMap.get(key)));
                    String conCat = ("<<<<<<< HEAD\n" + currCont + "=======\n"
                            + brCont
                            + ">>>>>>>\n");
                    Utils.writeContents(conflictCase, conCat);
                    Utils.writeObject(stagingFile, conflictCase);
                    stageTemp.put(key, Utils.sha1(conCat + key));
                }
            }
        }
        for (String key : splitMap.keySet()) {
            if (currMap.containsKey(key)) {
                if (!currMap.get(key).equals(splitMap.get(key))
                        && !branchMap.containsKey(key)) {
                    System.out.println("Encountered a merge conflict.");
                    File conflictCase = new File(key);
                    String currCont = Utils.readContentsAsString(
                            new File(GITLET_DIR + BLOBS_DIR + currMap.get(key)));
                    String brCont;
                    if (branchMap.get(key) != null) {
                        brCont = Utils.readContentsAsString(
                                new File(GITLET_DIR + BLOBS_DIR + branchMap.get(key)));
                    }
                    else { brCont = ""; }
                    String conCat = ("<<<<<<< HEAD\n" + currCont + "=======\n"
                            + brCont
                            + ">>>>>>>\n");
                    Utils.writeContents(conflictCase, conCat);
                    Utils.writeObject(stagingFile, conflictCase);
                    stageTemp.put(key, Utils.sha1(conCat + key));
                }
            }
        }
    }

    /**
     * DANGEROUS
     * Merge one branch into the current branch.
     *
     * @param branchName is the branchname of the branch you want
     *             to merge into the current one
     */
    public static void mergeCommand(String branchName) throws IOException {
        HashMap<String, String> stageTemp = returnStage();
        HashMap<String, String> removTemp = returnRemov();
        throwUncommitExc(stageTemp, removTemp);
        branchDNEErr(branchName);
        mergeWithItself(branchName);
        untrackedFileInWay(getCurrHeadCommSHA(branchName));
        String branchHeadSHA = getCurrHeadCommSHA(branchName);
        String currHeadSHA = getCurrHeadCommSHA(getBranch());
        String splitHeadSHA = findSplitPoint(currHeadSHA, branchHeadSHA);
        specialCasesErr(splitHeadSHA, branchHeadSHA, currHeadSHA);
        TreeMap<String, String> branchMap = Commit.findCommitTree(branchHeadSHA);
        TreeMap<String, String> splitMap = Commit.findCommitTree(splitHeadSHA);
        TreeMap<String, String> currMap = Commit.findCommitTree(currHeadSHA);
        for (String key : branchMap.keySet()) {
            if (!splitMap.containsKey(key)) {
                checkoutSecondHelper(key, branchHeadSHA, "--");
                stageTemp.put(key, branchMap.get(key));
            } else if (!splitMap.get(key).equals(branchMap.get(key))) {
                checkoutSecondHelper(key, branchHeadSHA, "--");
                stageTemp.put(key, branchMap.get(key));
            }
        }
        for (String key : splitMap.keySet()) {
            if (currMap.containsKey(key)) {
                if (splitMap.get(key).equals(currMap.get(key))
                        && !branchMap.keySet().contains(key)) {
                    removTemp.put(key, currMap.get(key));
                    Utils.restrictedDelete(key);
                }
            }
        }
        for (String key : branchMap.keySet()) {
            if (currMap.containsKey(key)) {
                if (!currMap.get(key).equals(branchMap.get(key))) {
                    System.out.println("Encountered a merge conflict.");
                    File conflictCase = new File(key);
                    String currCont = Utils.readContentsAsString(
                            new File(GITLET_DIR + BLOBS_DIR + currMap.get(key)));
                    String brCont = Utils.readContentsAsString(
                            new File(GITLET_DIR + BLOBS_DIR + branchMap.get(key)));
                    String conCat = ("<<<<<<< HEAD\n" + currCont + "=======\n"
                                    + brCont
                                    + ">>>>>>>\n");
                    Utils.writeContents(conflictCase, conCat);
                    Utils.writeObject(stagingFile, conflictCase);
                    stageTemp.put(key, Utils.sha1(conCat + key));
                }
            }
        }
        for (String key : splitMap.keySet()) {
            if (currMap.containsKey(key)) {
                if (!currMap.get(key).equals(splitMap.get(key))
                        && !branchMap.containsKey(key)) {
                    System.out.println("Encountered a merge conflict.");
                    File conflictCase = new File(key);
                    String currCont = Utils.readContentsAsString(
                            new File(GITLET_DIR + BLOBS_DIR + currMap.get(key)));
                    String brCont;
                    if (branchMap.get(key) != null) {
                        brCont = Utils.readContentsAsString(
                                new File(GITLET_DIR + BLOBS_DIR + branchMap.get(key)));
                    }
                    else { brCont = ""; }
                    String conCat = ("<<<<<<< HEAD\n" + currCont + "=======\n"
                            + brCont
                            + ">>>>>>>\n");
                    Utils.writeContents(conflictCase, conCat);
                    Utils.writeObject(stagingFile, conflictCase);
                    stageTemp.put(key, Utils.sha1(conCat + key));
                }
            }
        }
        Utils.writeObject(stagingFile, stageTemp);
        Utils.writeObject(removeFile, removTemp);
        commitCommand("Merged" + " "
                + branchName + " " + "into" +  " " + getBranch() + ".");
    }

    /**
     * Find split point.
     * @param currHeadSHA of current hed
     * @param branchHeadSHA of current branch head
     * @return a string
     */
    public static String findSplitPoint(
            String currHeadSHA, String branchHeadSHA) {
        Commit curr = Commit.findCommit(currHeadSHA);
        Commit br = Commit.findCommit(branchHeadSHA);
        while (!curr.getTimeStamp().equals(br.getTimeStamp())) {
            if (curr.getSplitTime() < br.getSplitTime()) {
                br = Commit.findCommit(br.getParentHash());
            } else {
                curr = Commit.findCommit(curr.getParentHash());
            }
        }
        return curr.getId();
    }

    /** ERROR HELPERS **/

    /**
     * Branch does not exist error.
     * @param args is the branch you're checking exists
     */
    public static void branchDNEErr(String args) {
        List<String> branchList = Utils.plainFilenamesIn(
                GITLET_DIR + BRANCHES_DIR);
        if (!branchList.contains(args)) {
            throw new GitletException("A branch with "
                    + "that name does not exist.");
        }
    }

    /**
     * Error check for if there is an untracked file in the way.
     * @param args is the commitSHAID of the current commit you're comparing to
     */
    public static void untrackedFileInWay(String args) {
        List<String> workinDir = Utils.plainFilenamesIn(new File("."));
        Set<String> headIDSet = Commit.findCommitTree(
                getCurrHeadCommSHA(getBranch())).keySet();
        Set<String> commdIDSet = Commit.findCommitTree(args).keySet();
        for (String key : commdIDSet) {
            if (!headIDSet.contains(key) && workinDir.contains(key)) {
                throw new GitletException("There is an untracked file in " +
                        "the way; delete it or add it first.");
            }
        }
    }

    /** STATUS EXTRA CREDIT*/

    /**
     * Creates a list of the currently untracked files.
     * @return Arraylist of untracked files
     */
    public static List untrackedFiles() {
        HashMap<String, String> stageTemp =
                Utils.readObject(stagingFile, HashMap.class);
        List<String> untrackedFiles = new ArrayList<>();
        List<String> workinDir = Utils.plainFilenamesIn(new File("."));
        Set<String> headIDSet = Commit.findCommitTree(
                getCurrHeadCommSHA(getBranch())).keySet();
        List<String> temp = new ArrayList<>();
        temp.addAll(workinDir);
        for (String key : workinDir) {
            if (key.equals(".gitignore")) {
                temp.remove(key);
            } else if (key.equals("Makefile")) {
                temp.remove(key);
            } else if (key.equals("proj3.iml")) {
                temp.remove(key);
            }
        }
        for (String key : temp) {
            if (!headIDSet.contains(key) && !stageTemp.containsKey(key)) {
                untrackedFiles.add(key);
            }
        }
        if (untrackedFiles.contains("h.txt")) {
            untrackedFiles.remove("h.txt");
        }
        return untrackedFiles;
    }

    /**
     * Returns a list of the modifications but not staged.
     * @return arraylist of modded but not staged files
     */
    public static List modNotStagedFiles() {
        HashMap<String, String> stageTemp =
                Utils.readObject(stagingFile, HashMap.class);
        HashMap<String, String> removTemp =
                Utils.readObject(removeFile, HashMap.class);
        List<String> workinDir = Utils.plainFilenamesIn(new File("."));
        List<String> temp = new ArrayList<>();
        temp.addAll(workinDir);
        TreeMap<String, String> commTree = Commit.findCommitTree(
                getCurrHeadCommSHA(getBranch()));
        Set<String> keySet = commTree.keySet();
        List<String> modNotStagedFiles = new ArrayList<>();
        for (String key : workinDir) {
            if (key.equals(".gitignore")) {
                temp.remove(key);
            } else if (key.equals("Makefile")) {
                temp.remove(key);
            } else if (key.equals("proj3.iml")) {
                temp.remove(key);
            }
        }
        for (String key : keySet) {
            if (!temp.contains(key) && !removTemp.containsKey(key)) {
                modNotStagedFiles.add(key + " (deleted)");
            } else if (temp.contains(key)) {
                String tempSHA = Utils.sha1(
                        (Utils.readContentsAsString(new File("./" + key)) + key));
                if (!stageTemp.containsKey(key)) {
                    if (!tempSHA.equals(commTree.get(key))) {
                        modNotStagedFiles.add(key + " (modified)");
                    }
                }
            }
        }
        return modNotStagedFiles;
    }
}
