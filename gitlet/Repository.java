package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Objects;

/** The class that will acts my tree structure
 * for gitlet and contains the code for all the diff
 * commands and other needed functions.
 * @author Jerome */
public class Repository implements Serializable {

    /** Creates a new Gitlet version-control system in the
     * current directory. This system will automatically start
     * with one commit: a commit that contains no files. It will have
     * a single branch: master, which initially points to this initial
     * commit, and master will be the current branch. */
    public Repository() {
        Commit initial = Commit.initCommit();
        File gitlet = new File(".gitlet");
        gitlet.mkdir();
        File commits = new File(".gitlet/commits");
        commits.mkdir();
        File staging = new File(".gitlet/staging");
        staging.mkdir();

        String code = initial.getUniversalID();
        File initialFile = new File(".gitlet/commits/" + code);
        Utils.writeContents(initialFile, (Object) Utils.serialize(initial));
        _headpointer = "master";
        _branches = new HashMap<String, String>();
        _branches.put("master", initial.getUniversalID());
        _stagingArea = new HashMap<String, String>();
        _untrackedFiles = new ArrayList<String>();
    }

    /** Starting at the current head commit,
     * display information about each commit backwards along the commit
     * tree until the initial commit, following the first parent commit
     * links, ignoring any second parents found in merge commits. For
     * every node in this history, the information displayed
     * is the commit id, the time the commit was made, and the commit
     * message. */
    public void printlog() {
        String head = header();
        while (head != null) {
            Commit first = uidToCommit(head);
            if (first.getParents() != null && first.getParents().length > 1) {
                System.out.println("===");
                System.out.println("commit " + head);
                String short1 = first.getParents()[0].substring(0, 7);
                String short2 = first.getParents()[1].substring(0, 7);
                System.out.println("Merge: " + short1 + " " + short2);
                System.out.println("Date: " + first.getTimestamp());
                System.out.println(first.getMessage());
                System.out.println();
            } else {
                System.out.println("===");
                System.out.println("commit " + head);
                System.out.println("Date: " + first.getTimestamp());
                System.out.println(first.getMessage());
                System.out.println();
            }
            head = first.getParentID();
        }
    }

    /** Takes in a String S.
     * @param newf a new file. */
    public void add(String newf) {
        File f = new File(newf);
        if (!f.exists()) {
            Utils.message("File does not exist.");
            throw new GitletException();
        }
        String fiHashid = Utils.sha1(Utils.readContentsAsString(f));
        Commit mostRecent = uidToCommit(header());
        HashMap<String, String> files = mostRecent.retrieveFiles();

        File stagingBlob = new File(".gitlet/staging/" + fiHashid);
        boolean b;
        if (files == null) {
            b = true;
        } else {
            b = false;
        }
        if (b || !files.containsKey(newf)
                || !files.get(newf).equals(fiHashid)) {
            _stagingArea.put(newf, fiHashid);
            String contents = Utils.readContentsAsString(f);
            Utils.writeContents(stagingBlob, contents);
        } else {
            if (stagingBlob.exists()) {
                _stagingArea.remove(newf);
            }
        }
        if (_untrackedFiles.contains(newf)) {
            _untrackedFiles.remove(newf);
        }
    }

    /**
     *Takes in a String MSG.
     */
    public void commit(String msg) {
        if (msg.trim().equals("")) {
            Utils.message("Please enter a commit message.");
            throw new GitletException();
        }
        Commit mostRecent = uidToCommit(header());
        HashMap<String, String> trackedFiles = mostRecent.retrieveFiles();

        if (trackedFiles == null) {
            trackedFiles = new HashMap<String, String>();
        }

        if (_stagingArea.size() != 0 || _untrackedFiles.size() != 0) {
            for (String fileName : _stagingArea.keySet()) {
                trackedFiles.put(fileName, _stagingArea.get(fileName));
            }
            for (String fileName : _untrackedFiles) {
                trackedFiles.remove(fileName);
            }
        } else {
            Utils.message("No changes added to the commit.");
            throw new GitletException();
        }
        String[] parent = new String[]{mostRecent.getUniversalID()};
        Commit newCommit = new Commit(msg, trackedFiles, parent, true);
        String s = newCommit.getUniversalID();
        File newCommFile = new File(".gitlet/commits/" + s);
        Utils.writeObject(newCommFile, newCommit);

        _stagingArea = new HashMap<String, String>();
        _untrackedFiles = new ArrayList<String>();
        _branches.put(_headpointer, newCommit.getUniversalID());
    }

    /** Exactly like the regular commit function, but
     * used for merge commits, takes in a String MSG, and
     * a set of PARENTS. */
    public void commit(String msg, String[] parents) {
        if (msg.trim().equals("")) {
            Utils.message("Please enter a commit message.");
            throw new GitletException();
        }
        Commit mostRecent = uidToCommit(header());
        HashMap<String, String> trackedFiles = mostRecent.retrieveFiles();

        if (trackedFiles == null) {
            trackedFiles = new HashMap<String, String>();
        }

        if (_stagingArea.size() != 0 || _untrackedFiles.size() != 0) {
            for (String fileName : _stagingArea.keySet()) {
                trackedFiles.put(fileName, _stagingArea.get(fileName));
            }
            for (String fileName : _untrackedFiles) {
                trackedFiles.remove(fileName);
            }
        } else {
            Utils.message("No changes added to the commit.");
            throw new GitletException();
        }
        Commit newCommit = new Commit(msg, trackedFiles, parents, true);
        String s = newCommit.getUniversalID();
        File newCommFile = new File(".gitlet/commits/" + s);
        Utils.writeObject(newCommFile, newCommit);

        _untrackedFiles = new ArrayList<String>();
        _stagingArea = new HashMap<String, String>();
        _branches.put(_headpointer, newCommit.getUniversalID());
    }

    /** Takes in a String[] ARGS.
     */
    public void checkout(String[] args) {
        String commID;
        String fileName;
        if (args.length == 2 && args[0].equals("--")) {
            fileName = args[1];
            commID = header();
        } else if (args.length == 3 && args[1].equals("--")) {
            commID = args[0];
            fileName = args[2];
        } else {
            Utils.message("Incorrect operands");
            throw new GitletException();
        }
        commID = unShortenID(commID);
        Commit comm = uidToCommit(commID);
        HashMap<String, String> trackedFiles = comm.retrieveFiles();
        if (trackedFiles.containsKey(fileName)) {
            File f = new File(fileName);
            String p = ".gitlet/staging/";
            String blobFileName = p + trackedFiles.get(fileName);
            File g = new File(blobFileName);
            String contents = Utils.readContentsAsString(g);
            Utils.writeContents(f, contents);
        } else {
            Utils.message("File does not exist in that commit.");
            throw new GitletException();
        }
    }

    /** Takes in a shortened String ID and returns a String
     * of the full length ID. */
    private String unShortenID(String id) {
        if (id.length() == Utils.UID_LENGTH) {
            return id;
        }
        File commitHolder = new File(".gitlet/commits");
        File[] commits = commitHolder.listFiles();
        assert commits != null;
        for (File file : commits) {
            if (file.getName().contains(id)) {
                return file.getName();
            }
        }
        Utils.message("No commit with that id exists.");
        throw new GitletException();
    }

    /** This is the third use case for checkout.
     * It takes in a branch name instead.
     * @param branchName a branch.*/
    public void checkout(String branchName) {
        if (!_branches.containsKey(branchName)) {
            Utils.message("No such branch exists.");
            throw new GitletException();
        }
        if (_headpointer.equals(branchName)) {
            String s = "No need to checkout the current branch.";
            Utils.message(s);
            throw new GitletException();
        }
        String brnchID = _branches.get(branchName);
        Commit com1 = uidToCommit(brnchID);
        HashMap<String, String> files = com1.retrieveFiles();
        String pdString = System.getProperty("user.dir");
        File pd = new File(pdString);
        checkForUntracked(pd);
        for (File file : Objects.requireNonNull(pd.listFiles())) {
            if (files == null) {
                Utils.restrictedDelete(file);
            } else {
                boolean beta = !files.containsKey(file.getName());
                if (beta && !file.getName().equals(".gitlet")) {
                    Utils.restrictedDelete(file);
                }
            }
        }
        if (files != null) {
            for (String file : files.keySet()) {
                String g = ".gitlet/staging/" + files.get(file);
                File f = new File(g);
                String contents = Utils.readContentsAsString(f);
                Utils.writeContents(new File(file), contents);
            }
        }
        _stagingArea = new HashMap<String, String>();
        _untrackedFiles = new ArrayList<String>();
        _headpointer = branchName;
    }

    /** This function takes in the present working directory
     * PWD and will determine if there are untracked files
     * that mean that this checkout or Merge operation can't
     * continue.
     * @param dir a file.*/
    private void checkForUntracked(File dir) {
        String mm;
        mm = "There is an untracked file in the way; ";
        mm += "delete it or add it first.";
        Commit mostRecent = uidToCommit(header());
        HashMap<String, String> trackedFiles = mostRecent.retrieveFiles();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (trackedFiles == null) {
                if (Objects.requireNonNull(dir.listFiles()).length > 1) {
                    Utils.message(mm);
                    throw new GitletException();
                }
            } else {
                boolean b = !trackedFiles.containsKey(file.getName());
                boolean c = !_stagingArea.containsKey(file.getName());
                if (b && !file.getName().equals(".gitlet") && c) {
                    Utils.message(mm);
                    throw new GitletException();
                }
            }
        }
    }

    /** Un-stage the file if it is currently staged. If the file is
     * tracked in the current commit, mark it to indicate that it is
     * not to be included in the next commit, and remove
     * from the working directory if the user has not already done so
     ** Takes in a String ARG.
     * @param name a file.*/
    public void remove(String name) {
        File file = new File(name);
        Commit current = uidToCommit(header());
        HashMap<String, String> trackedFiles = current.retrieveFiles();
        boolean marked = false;
        if (!file.exists() && !current.retrieveFiles().containsKey(name)) {
            Utils.message("File does not exist.");
            throw new GitletException();
        }
        if (_stagingArea.containsKey(name)) {
            _stagingArea.remove(name);
            marked = true;
        }
        if (trackedFiles != null && trackedFiles.containsKey(name)) {
            _untrackedFiles.add(name);
            File temp = new File(name);
            Utils.restrictedDelete(temp);
            marked = true;
        }
        if (!marked) {
            Utils.message("No reason to remove the file.");
            throw new GitletException();
        }
    }

    /** Takes no arg and prints out all of the commits
     * that have ever occured. */
    public void globLog() {
        File folder = new File(".gitlet/commits");
        File[] allcommits = folder.listFiles();
        for (File file : allcommits) {
            printCommit(file.getName());
        }
    }

    /** The find method that will locate the commit with
     * the given message.
     * @param message a message.
     */
    public void find(String message) {
        File folder = new File(".gitlet/commits");
        File[] commits = folder.listFiles();
        boolean located = false;
        for (File file : commits) {
            Commit com = uidToCommit(file.getName());
            if (com.getMessage().equals(message)) {
                System.out.println(file.getName());
                located = true;
            }
        }
        if (!located) {
            Utils.message("Found no commit with that message.");
            throw new GitletException();
        }
    }

    /** Give the status of a repo. */
    public void status() {
        System.out.println("=== Branches ===");
        Object[] keys = _branches.keySet().toArray();
        Arrays.sort(keys);
        for (Object brch : keys) {
            if (brch.equals(_headpointer)) {
                System.out.println("*" + brch);
            } else {
                System.out.println(brch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        Object[] stagedfls = _stagingArea.keySet().toArray();
        Arrays.sort(stagedfls);
        for (Object staged : stagedfls) {
            System.out.println(staged);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        Object[] untrackfls = _untrackedFiles.toArray();
        Arrays.sort(untrackfls);
        for (Object removed : untrackfls) {
            System.out.println(removed);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /** Makes a new branch in the repo tree.
     * @param name a branch.*/
    public void branch(String name) {
        if (!_branches.containsKey(name)) {
            _branches.put(name, header());
        } else {
            Utils.message("A branch with that name already exists.");
            throw new GitletException();
        }
    }

    /** removes the branch that has the given name.
     * @param name branch name.*/
    public void rmbranch(String name) {
        if (_headpointer.equals(name)) {
            Utils.message("Cannot remove the current branch.");
            throw new GitletException();
        }
        if (_branches.containsKey(name)) {
            _branches.remove(name);
        } else {
            Utils.message("A branch with that name does not exist.");
            throw new GitletException();
        }
    }

    /**Checks out all files under the commit.
     *Remove files that aren't in the commit. Resets head to
     * that commit.Stage area cleared.
     * @param idstr an id string. */
    public void reset(String idstr) {
        idstr = unShortenID(idstr);
        Commit comstr = uidToCommit(idstr);
        HashMap<String, String> collection = comstr.retrieveFiles();
        File dir = new File(System.getProperty("user.dir"));
        checkForUntracked(dir);

        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (!collection.containsKey(file.getName())) {
                Utils.restrictedDelete(file);
            }
        }
        for (String file : collection.keySet()) {
            File f = new File(".gitlet/staging/" + collection.get(file));
            String contents = Utils.readContentsAsString(f);
            Utils.writeContents(new File(file), contents);
        }
        _stagingArea = new HashMap<String, String>();
        _branches.put(_headpointer, idstr);
    }

    /** Takes care of the merge operation.
     * @param brname a branch name. */
    public void merge(String brname) {
        if (_stagingArea.size() != 0 || _untrackedFiles.size() != 0) {
            Utils.message("You have uncommitted changes.");
            throw new GitletException();
        }
        if (!_branches.containsKey(brname)) {
            Utils.message("A branch with that name does not exist.");
            throw new GitletException();
        }
        if (brname.equals(_headpointer)) {
            Utils.message("Cannot merge a branch with itself.");
            throw new GitletException();
        }
        String splt = splitPoint(brname, _headpointer);
        if (splt.equals(_branches.get(brname))) {
            Utils.message("Given branch is an ancestor of the current branch.");
            return;
        }
        if (splt.equals(_branches.get(_headpointer))) {
            _branches.put(_headpointer, _branches.get(brname));
            Utils.message("Current branch fast-forwarded.");
            return;
        }

        Commit splCommit = uidToCommit(splt);
        HashMap<String, String> splitpoints = splCommit.retrieveFiles();
        midmerg(brname);
        Commit currComm = uidToCommit(header());
        HashMap<String, String> current = currComm.retrieveFiles();
        Commit givencom = uidToCommit(_branches.get(brname));
        HashMap<String, String> givenset = givencom.retrieveFiles();


        for (String fileName : givenset.keySet()) {
            if (!splitpoints.containsKey(fileName)) {
                if (!current.containsKey(fileName)) {
                    String b = _branches.get(brname);
                    checkout(new String[] {b, "--", fileName});
                    _stagingArea.put(fileName, givenset.get(fileName));
                } else if (!givenset.containsKey(fileName)) {
                    continue;
                } else if (modchk(fileName, givenset, current)) {
                    String p = ".gitlet/staging/";
                    File charlie = new File(p + current.get(fileName));
                    File gamma = new File(p + givenset.get(fileName));
                    String contents = "<<<<<<< HEAD\n";
                    contents += Utils.readContentsAsString(charlie);
                    contents += "=======\n";
                    contents += Utils.readContentsAsString(gamma) + ">>>>>>>";
                    Utils.writeContents(new File(fileName), contents);
                    add(fileName);
                    Utils.message("Encountered a merge conflict.");
                }
            }
        }
        String[] parents = new String[]{header(), _branches.get(brname)};
        commit("Merged " + brname + " into " + _headpointer + ".", parents);
    }

    /** Splitting up the merge. Need a BRANCHNAME. */
    private void midmerg(String branchName) {
        String split = splitPoint(branchName, _headpointer);
        Commit splitCommit = uidToCommit(split);
        HashMap<String, String> splitFiles = splitCommit.retrieveFiles();
        Commit currComm = uidToCommit(header());
        HashMap<String, String> current = currComm.retrieveFiles();
        Commit givenComm = uidToCommit(_branches.get(branchName));
        HashMap<String, String> givenset = givenComm.retrieveFiles();

        String pwdString = System.getProperty("user.dir");
        File pwd = new File(pwdString);
        checkForUntracked(pwd);

        for (String fileName : splitFiles.keySet()) {
            boolean presentInGiven = givenset.containsKey(fileName);
            boolean modInCurr = modchk(fileName, splitFiles, current);
            boolean modInGiven = modchk(fileName, splitFiles, givenset);
            if (!modInCurr) {
                if (!presentInGiven) {
                    Utils.restrictedDelete(new File(fileName));
                    remove(fileName);
                    continue;
                }
                if (modInGiven) {
                    String b = _branches.get(branchName);
                    checkout(new String[]{b, "--", fileName});
                    add(fileName);
                }
            }
            if (modInCurr && modInGiven) {
                if (modchk(fileName, givenset, current)) {
                    mergCon(branchName, fileName);
                }
            }
        }
    }

    /** This has a BRANCHNAME and a FILENAME. */
    private void mergCon(String branchName, String fileName) {
        String split = splitPoint(branchName, _headpointer);
        Commit splicom = uidToCommit(split);
        HashMap<String, String> splitFiles = splicom.retrieveFiles();
        Commit currComm = uidToCommit(header());
        HashMap<String, String> current = currComm.retrieveFiles();
        Commit givenComm = uidToCommit(_branches.get(branchName));
        HashMap<String, String> given = givenComm.retrieveFiles();
        String p = ".gitlet/staging/";
        File charls;
        String cContents;
        if (current.containsKey(fileName)) {
            charls = new File(p + current.get(fileName));
            cContents = Utils.readContentsAsString(charls);
        } else {
            charls = null;
            cContents = "";
        }
        File gar;
        String gContents;
        if (given.containsKey(fileName)) {
            gar = new File(p + given.get(fileName));
            gContents = Utils.readContentsAsString(gar);
        } else {
            gar = null;
            gContents = "";
        }
        String contents = "<<<<<<< HEAD\n";
        contents += cContents;
        contents += "=======\n" + gContents;
        contents += ">>>>>>>\n";
        Utils.writeContents(new File(fileName), contents);
        add(fileName);
        Utils.message("Encountered a merge conflict.");
    }
    /** Takes in two branch names, BRANCH1 and BRANCH2. Returns the
     * SHA ID of the common ancestor commit. */
    private String splitPoint(String branch1, String branch2) {
        ArrayList<String> bnch1coms = new ArrayList<String>();
        ArrayList<String> bnch2coms = new ArrayList<String>();

        String parn1 = _branches.get(branch1);
        String parn2 = _branches.get(branch2);

        while (parn1 != null) {
            bnch1coms.add(parn1);
            Commit com1 = uidToCommit(parn1);
            parn1 = com1.getParentID();
        }
        while (parn2 != null) {
            bnch2coms.add(parn2);
            Commit com2 = uidToCommit(parn2);
            parn2 = com2.getParentID();
        }
        for (String cm : bnch1coms) {
            if (bnch2coms.contains(cm)) {
                return cm;
            }
        }
        return "";
    }

    /** Returns a boolean if the file has been modified from
     * branch a1 to branch a2.
     * @param freq  a file.
     * @param a1 one branch
     * @param a2  the other branch. */
    boolean modchk(String freq, HashMap<String, String> a1,
                   HashMap<String, String> a2) {
        if (a1.containsKey(freq) && a2.containsKey(freq)) {
            String hashF1 = a1.get(freq);
            String hashF2 = a2.get(freq);
            if (!hashF1.equals(hashF2)) {
                return true;
            }
        } else if (a1.containsKey(freq) || a2.containsKey(freq)) {
            return true;
        }
        return false;
    }

    /** The helper function that takes in an UID and
     * returns the object that corresponds to that
     * id. */
    public Commit uidToCommit(String uid) {
        File f1 = new File(".gitlet/commits/" + uid);
        if (f1.exists()) {
            return Utils.readObject(f1, Commit.class);
        } else {
            Utils.message("No commit with that id exists.");
            throw new GitletException();
        }
    }

    /** Takes in a UID for a commit, and prints out the commit,
     * what it prints out depends on whether it is a merge
     * commit or a regular commit. */
    public void printCommit(String uid) {
        Commit comm = uidToCommit(uid);
        if (comm.getParents() != null && comm.getParents().length > 1) {
            System.out.println("===");
            System.out.println("commit " + uid);
            String short1 = comm.getParents()[0].substring(0, 7);
            String short2 = comm.getParents()[1].substring(0, 7);
            System.out.println("Merge: " + short1 + " " + short2);
            System.out.println("Date: " + comm.getTimestamp());
            System.out.println(comm.getMessage());
            System.out.println();
        } else {
            System.out.println("===");
            System.out.println("commit " + uid);
            System.out.println("Date: " + comm.getTimestamp());
            System.out.println(comm.getMessage());
            System.out.println();
        }
    }

    /** Returns the uid of the current head which
     * corresponds to the head branch. */
    public String header() {
        return _branches.get(_headpointer);
    }

    /** Overseer of entire tree structure, each branch has a name (String)
     * and a hash ID of its current position.*/
    private HashMap<String, String> _branches;

    /** The head pointer that corresponds to the branch that actually will be
     * pointing at the commit that we want . */
    private String _headpointer;

    /** Staging Area, maps the name of the file. */
    private HashMap<String, String> _stagingArea;

    /** Untracked files are like the opposite of the Staging Area,
     * these are files that WERE tracked before, and now, for the
     * next commit, they're not going to be added. */
    private ArrayList<String> _untrackedFiles;
}
