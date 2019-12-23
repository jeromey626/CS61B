package enigma;


import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Jerome Chen
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        String holder = cycles.trim();
        holder = holder.replace(")", "");
        holder = holder.replace("(", "");
        thecycles = holder.split(" ");
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        String[] newC = new String[thecycles.length + 1];
        for (int a = 0; a < thecycles.length; a++) {
            newC[a] = thecycles[a];
        }
        newC[thecycles.length + 1] = cycle;
        thecycles = newC;
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the value of P modulo the input SIZE.
     *@param a the first number for mod
     * @param b the second number to be used for mod*/
    int mod(int a, int b) {
        int c = a % b;
        if (c < 0) {
            c += b;
        }
        return c;
    }


    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char let = _alphabet.toChar(wrap(p));
        char newlet = '0';
        for (int a = 0; a < thecycles.length; a++) {
            for (int b = 0; b < thecycles[a].length(); b++) {
                if (thecycles[a].charAt(b) == let) {
                    newlet = thecycles[a].charAt(mod((b + 1),
                             thecycles[a].length()));
                    return _alphabet.toInt(newlet);
                }
            }
        }
        return  p;
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char n = _alphabet.toChar(wrap(c));
        char newChar = '0';
        for (int q = 0; q < thecycles.length; q++) {
            for (int w = 0; w < thecycles[q].length(); w++) {
                if (thecycles[q].charAt(w) == n) {
                    newChar = thecycles[q].charAt(mod((w - 1),
                            thecycles[q].length()));
                    return _alphabet.toInt(newChar);
                }
            }
        }
        return c;
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int ind = _alphabet.toInt(p);
        return _alphabet.toChar(permute(ind));
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int ind = _alphabet.toInt(c);
        return _alphabet.toChar(invert(ind));
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        int cnt = 0;
        int alphsize = _alphabet.size();
        for (int r = 0; r < thecycles.length; r++) {
            cnt += thecycles[r].length();
        }
        if (cnt == alphsize) {
            return true;
        }
        return false;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;
    /** An array of strings, each string is a cycle that tells
     * how a letter would be converted. */
    private String[] thecycles;
}
