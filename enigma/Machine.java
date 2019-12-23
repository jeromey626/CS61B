package enigma;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Jerome
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _nRotors = numRotors;
        _nPawls = pawls;
        ArrayList<String> rotorNames = new ArrayList<>();
        for (Rotor rot : allRotors) {
            if (rotorNames.contains(rot.name())) {
                throw new EnigmaException("no rotor repeating.");
            }
            if (rot.alphabet() != _alphabet) {
                throw new EnigmaException("Rotors and machine must "
                        + "use the same alphabet.");
            }
            rotorNames.add(rot.name());
        }
        _theRotors = allRotors;

    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _nRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _nPawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        setrotors = new Rotor[numRotors()];
        HashMap<String, Rotor> mapRotors = new HashMap<String, Rotor>();
        for (Rotor rotor : _theRotors) {
            mapRotors.put(rotor.name().toUpperCase(), rotor);
        }
        for (int a = 0; a < rotors.length; a++) {
            String key = rotors[a].toUpperCase();
            if (mapRotors.containsKey(key)) {
                setrotors[a] = mapRotors.get(key);
            }
        }

        if (rotors.length != setrotors.length) {
            throw new EnigmaException("the amount of rotors to be added "
                    + "must match the capacity of the machine");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != (numRotors() - 1)) {
            throw new EnigmaException("Wheel settings wrong length");
        }


        for (int s = 1; s < setrotors.length; s++) {
            if (!_alphabet.contains(setting.charAt(s - 1))) {
                throw new EnigmaException("Bad character in "
                        + "wheel settings");
            }
            setrotors[s].set(setting.charAt(s - 1));
        }
    }
    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }


    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        boolean[] atnotch = new boolean[numRotors()];
        boolean[] rotated = new boolean[numRotors()];

        for (int a = numRotors() - 1; a > 0; a--) {
            atnotch[a] = setrotors[a].atNotch();
            rotated[a] = false;
        }
        setrotors[numRotors() - 1].advance();
        rotated[numRotors() - 1] = true;
        for (int a = numRotors() - 2; a > 0; a--) {
            if (setrotors[a].rotates()) {
                if (atnotch[a + 1]) {
                    setrotors[a].advance();
                    rotated[a] = true;
                    if (!rotated[a + 1]) {
                        setrotors[a + 1].advance();
                        rotated[a + 1] = true;
                    }
                }
            }
        }
        int oz = _plugboard.permute(c);
        for (int j = setrotors.length - 1; j >= 0; j--) {
            oz = setrotors[j].convertForward(oz);
        }
        for (int k = 1; k < setrotors.length; k += 1) {
            oz = setrotors[k].convertBackward(oz);
        }
        oz = _plugboard.invert(oz);
        return oz;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String newmsg = "";
        for (int s = 0; s < msg.length(); s++) {
            int changed = _alphabet.toInt(msg.charAt(s));
            char converted = _alphabet.toChar(convert(changed));
            newmsg += converted;
        }
        return newmsg;
    }

    /** Returns the array of rotors. */
    public Rotor[] getRotorList() {
        return setrotors;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;
    /** additional. */
    private int _nRotors;
    /** additional. */
    private int _nPawls;
    /** additional. */
    private Collection<Rotor> _theRotors;
    /** additional. */
    private Permutation _plugboard;
    /** additional. */
    private Rotor[] setrotors;
}

