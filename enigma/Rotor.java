package enigma;

import static enigma.EnigmaException.*;

/** Superclass that represents a rotor in the enigma machine.
 *  @author Jerome
 */
class Rotor {

    /** A rotor named NAME whose permutation is given by PERM. */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        _setting = 0;
    }

    /** Return my name. */
    String name() {
        return _name;
    }

    /** Return my alphabet. */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /** Return my permutation. */
    Permutation permutation() {
        return _permutation;
    }

    /** Return the size of my alphabet. */
    int size() {
        return _permutation.size();
    }

    /** Return true iff I have a ratchet and can move. */
    boolean rotates() {
        return false;
    }

    /** Return true iff I reflect. */
    boolean reflecting() {
        return false;
    }

    /** Return my current setting. */
    int setting() {
        return _setting;
    }

    /** Set setting() to POSN. */
    void set(int posn) {
        _setting = posn;
    }

    /** Set setting() to character CPOSN.*/
    void set(char cposn) {
        _setting = alphabet().toInt(cposn);
    }

    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {
        if (p < 0) {
            throw new EnigmaException("character out of range");
        }
        int res = _permutation.permute(_permutation.wrap(p + _setting));
        return _permutation.wrap((res - _setting));
    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation (need to fix). */
    int convertBackward(int e) {
        if (e < 0) {
            throw new EnigmaException("character out of range");
        }
        int res = _permutation.invert(_permutation.wrap(e + _setting));
        return _permutation.wrap(res - _setting);
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        return false;
    }

    /** Advance me one position, if possible. By default, does nothing. */
    void advance() {
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /** My name. */
    private final String _name;

    /** The permutation implemented by this rotor in its 0 position. */
    private Permutation _permutation;

    /** The setting implemented by the roto at its current state. */
    private int _setting;

    /** Return the value of P modulo the input SIZE.
     * @param a the first number for mod
     * @param b the second number for mod. */
    int mod(int a, int b) {
        int c = a % b;
        if (c < 0) {
            c += b;
        }
        return c;
    }
}
