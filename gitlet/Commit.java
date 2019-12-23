package gitlet;
import java.util.Date;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;

/** Defines what a class is and defines all
 * the individual parts of details of it.
 * @author Jerome Chen */
public class Commit implements Serializable {

    /** A commit is initialized.
     * @param msg the message.
     * @param files the files in a folder.
     * @param parents the parent commits.
     * @param update a boolean.
     */
    public Commit(String msg, HashMap<String,
            String> files, String[] parents, boolean update) {
        _msg = msg;
        _files = files;
        _parents = parents;
        Date dateObj;
        if (update) {
            dateObj = new Date();
            _time = DATE_FORMAT.format(dateObj) + " -0800";
        } else {
            _time = "Wed Dec 31 16:00:00 1969 -0800";
        }
        _universalID = hashCommit();
    }

    /** This function will hash the current commit based off
     * of the commit message, files, timestamp, and parents.
     * To return a hash. */
    public String hashCommit() {
        String files;
        if (_files != null) {
            files = _files.toString();
        } else {
            files = "";
        }
        String parents = Arrays.toString(_parents);
        return Utils.sha1(_msg, files, _time, parents);
    }

    /** Returns one to create an initial commit easily.*/
    public static Commit initCommit() {
        return new Commit("initial commit", null, null, false);
    }

    /** Returns the commit message of this
     * particular commit. */
    public String getMessage() {
        return _msg;
    }

    /** Returns all of the files that belong to a
     * particular commit. */
    public HashMap<String, String> retrieveFiles() {
        return _files;
    }

    /** Returns the timestamp of this particular
     * commit.  */
    public String getTimestamp() {
        return _time;
    }

    /** Returns the ID of the first parent
     * of this particular commit. */
    public String getParentID() {
        if (_parents != null) {
            return _parents[0];
        }
        return null;
    }

    /** Returns you to get the whole parents set from
     * this particular commit. */
    public String[] getParents() {
        return _parents;
    }

    /** Returns you to get the unviersal ID from the
     * particular commit. */
    public String getUniversalID() {
        return _universalID;
    }


    /** The commit message.*/
    private String _msg;

    /** The date of the commit.*/
    private String _time;

    /** A list of strings of hashes of Blobs that are being
     * tracked.*/
    private HashMap<String, String> _files;

    /** An array of Hashes of parents. */
    private String[] _parents;

    /** The hash of this commit. */
    private String _universalID;

    /** The date format. */
    public static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");
}
