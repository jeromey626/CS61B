package gitlet;
import java.io.File;
import java.util.Arrays;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Jerome Chen
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        try {
            if (args.length == 0) {
                Utils.message("Please enter a command.");
                throw new GitletException();
            }
            File repop = new File(System.getProperty("user.dir")
                    + "/.gitlet");
            if (validCommand(args[0])) {
                String[] operands = Arrays.copyOfRange(args, 1, args.length);
                if (repop.exists()) {
                    theRepo = recoverMyRepo();
                    execute(args, operands);
                    File og = new File(".gitlet/myrepo");
                    Utils.writeObject(og, theRepo);
                } else {
                    if (args[0].equals("init")) {
                        File og = new File(".gitlet/myrepo");
                        Utils.writeObject(og, new Repository());
                    } else {
                        String s = "Not in an initialized "
                                + "Gitlet directory.";
                        Utils.message(s);
                        throw new GitletException();
                    }
                }
            } else {
                Utils.message("No command with that name exists.");
                throw new GitletException();
            }
        } catch (GitletException e) {
            System.exit(0);
        }
    }

    /** Takes in a string ARG word, will return whether or not
     * it is a valid command. */
    private static boolean validCommand(String arg) {
        for (String command: commands) {
            if (arg.equals(command)) {
                return true;
            }
        }
        return false;
    }

    /** Takes in String[] ARGS and String OPERANDS. */
    private static void execute(String[] args, String[] operands) {
        switch (args[0]) {
        case "init":
            Utils.message("A Gitlet version-control system"
                    + "already exists in the current directory.");
            throw new GitletException();
        case "log":
            theRepo.printlog();
            break;
        case "commit":
            theRepo.commit(operands[0]);
            break;
        case "add":
            theRepo.add(operands[0]);
            break;
        case "checkout":
            if (operands.length != 1) {
                theRepo.checkout(operands);
            } else {
                theRepo.checkout(operands[0]);
            }
            break;
        case "merge":
            theRepo.merge(operands[0]);
            break;
        case "reset":
            theRepo.reset(operands[0]);
            break;
        case "rm-branch":
            theRepo.rmbranch(operands[0]);
            break;
        case "branch":
            theRepo.branch(operands[0]);
            break;
        case "status":
            theRepo.status();
            break;
        case "find":
            theRepo.find(operands[0]);
            break;
        case "global-log":
            theRepo.globLog();
            break;
        case "rm":
            theRepo.remove(operands[0]);
            break;
        default:
            Utils.message("something wrong");
        }
    }
    /** Will do the work of actually saving my repo. It
     * returns the existing repo, assuming
     * that there is one. */
    public static Repository recoverMyRepo() {
        File mr =  new File(OGPATH);
        return Utils.readObject(mr, Repository.class);
    }

    /** Array of possible valid commands. */
    private static String[] commands = new String[] {
        "init", "add",
        "commit", "rm", "log", "global-log",
        "find", "status", "checkout",
        "branch", "rm-branch", "reset", "merge"};

    /** The thing that controls everything. */
    private static Repository theRepo;

    /** Path to that Repo's file. */
    private static final String OGPATH = ".gitlet/myrepo";
}
