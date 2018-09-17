package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.List;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  Collaborators: Nidhi Kakulawaram, Anastasia Scott, Vish Pillay
 *  @author Mariel Aquino
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    @SuppressWarnings("unchecked")
    public static void main(String... args) throws IOException {
        try {
            if (args.length == 0) {
                System.out.println("Please enter a command.");
                System.exit(0);
            }
            notInitialized(args);
            switch (args[0]) {
            case "init":
                Commands.initCommand();
                break;
            case "add":
                Commands.addCommand(args[1]);
                break;
            case "commit":
                Commands.commitCommand(args[1]);
                break;
            case "rm":
                Commands.rmCommand(args[1]);
                break;
            case "log":
                Commands.logCommand();
                break;
            case "global-log":
                Commands.globalLogCommand();
                break;
            case "find":
                Commands.findCommand(args[1]);
                break;
            case "status":
                Commands.statusCommand();
                break;
            case "checkout":
                checkoutCase(args);
                break;
            case "branch":
                Commands.branchCommand(args[1]);
                break;
            case "rm-branch":
                Commands.rmBranchCommand(args[1]);
                break;
            case "reset":
                if (args[1].length() < 4 * 5 * 2) {
                    Commands.resetCommand(shortUID(args[1]));
                }
                Commands.resetCommand(args[1]);
                break;
            case "merge":
                Commands.mergeCommand(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
            }
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    /** Shorten the method.
     * @param args of main
     * @throws IOException
     */
    public static void checkoutCase(String... args) throws IOException {
        if (args.length == 3) {
            Commands.checkoutFirstHelper(args[2]);
        } else if (args.length == 4) {
            Commands.checkoutSecondHelper(args[3], args[1], args[2]);
        } else if (args.length == 2) {
            Commands.checkoutThirdHelper(args[1]);
        }
    }

    /** Shorten the main method.
     * @param args of main
     */
    public static void notInitialized(String... args) {
        File dir = new File(".gitlet/");
        if (!dir.exists() && !args[0].equals("init")) {
            throw new GitletException("Not in an "
                    + "initialized gitlet directory.");

        }
    }

    /**
     * For the shortened reset option.
     * @param args commit ID
     * @return a commit ID
     */
    public static String shortUID(String args) {
        List<String> allCommits = Utils.plainFilenamesIn(
                new File(".gitlet/.commits"));
        for (String commit : allCommits) {
            if (commit.contains(args)) {
                return commit;
            }
        }
        return "hi";
    }

}


