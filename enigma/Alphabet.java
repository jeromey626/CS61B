package enigma;
import static enigma.EnigmaException.*;


/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Jerome
 */
class Alphabet {
    /** A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        thechars = chars;
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return thechars.length();
    }

    /** Returns true if preprocess(CH) is in this alphabet. */
    boolean contains(char ch) {
        for (int e = 0; e < size(); e++) {
            if (thechars.charAt(e) == ch) {
                return true;
            }
        }
        return false;
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        if (index >= size() || index < 0) {
            throw error("character index isn't in range");
        }
        return thechars.charAt(index);
    }

    /** Returns the index of character preprocess(CH), which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        for (int z = 0; z < size(); z++) {
            if (thechars.charAt(z) == ch) {
                return z;
            }
        }
        throw new EnigmaException("character not in alphabet");
    }

    /** additional field. */
    private String thechars;
}
