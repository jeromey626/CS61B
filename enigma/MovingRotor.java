package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Jerome
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initially in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        allnotches = notches;
        _permutation = perm;
    }

    @Override
    boolean rotates() {
        return true;
    }
    @Override
    boolean atNotch() {
        for (int p = 0; p < allnotches.length(); p++) {
            if (this.setting() == alphabet().toInt(allnotches.charAt(p))) {
                return true;
            }
        }
        return false;
    }

    @Override
    void advance() {
        set(permutation().wrap(setting() + 1));
    }

    /** String containing notches of the rotor. */
    private String allnotches;
    /** String containing notches of the rotor. */
    private Permutation _permutation;

}
