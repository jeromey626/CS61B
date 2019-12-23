package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import java.util.HashMap;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Jerome
 */
public class MovingRotorTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Rotor rotor;
    private String alpha = UPPER_STRING;

    /** Check that rotor has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkRotor(String testId,
                            String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, rotor.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d (%c)", ci, c),
                         ei, rotor.convertForward(ci));
            assertEquals(msg(testId, "wrong inverse of %d (%c)", ei, e),
                         ci, rotor.convertBackward(ei));
        }
    }

    /** Set the rotor to the one with given NAME and permutation as
     *  specified by the NAME entry in ROTORS, with given NOTCHES. */
    private void setRotor(String name, HashMap<String, String> rotors,
                          String notches) {
        rotor = new MovingRotor(name, new Permutation(rotors.get(name), UPPER),
                                notches);
    }

    /* ***** TESTS ***** */

    @Test
    public void checkRotorAtA() {
        setRotor("I", NAVALA, "");
        checkRotor("Rotor I (A)", UPPER_STRING, NAVALA_MAP.get("I"));
    }

    @Test
    public void checkRotorAdvance() {
        setRotor("I", NAVALA, "");
        rotor.advance();
        checkRotor("Rotor I advanced", UPPER_STRING, NAVALB_MAP.get("I"));
    }

    @Test
    public void checkRotorSet() {
        setRotor("I", NAVALA, "");
        rotor.set(25);
        checkRotor("Rotor I set", UPPER_STRING, NAVALZ_MAP.get("I"));
    }

    @Test
    public void advancetest() {
        Alphabet standard = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Permutation test1 = new Permutation("(ABDEKLMNOP) (CFGHIJQRS) "
                + "(TUVWXYZ)", standard);
        Rotor rotorone = new MovingRotor("A", test1, "");
        rotorone.advance();
        int a = rotorone.convertForward(1);
        assertEquals(standard.toChar(a), 'E');
        int b = rotorone.convertBackward(0);
        assertEquals(standard.toChar(b), 'Z');

        rotorone.advance();
        assertEquals(rotorone.setting(), 2);
        int v = rotorone.convertForward(16);
        assertEquals(standard.toChar(v), 'A');
    }

    @Test
    public void setTest() {
        Alphabet standard = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Permutation test1 = new Permutation("(ABDEKLMNOP) (CFGHIJQRS) "
                + "(TUVWXYZ)", standard);
        Rotor rotorone = new MovingRotor("A", test1, "");

        rotorone.set(2);
        int q = rotorone.convertForward(16);
        assertEquals(standard.toChar(q), 'A');

        rotorone.set('A');
        int w = rotorone.convertForward(0);
        assertEquals(w, 1);

        rotorone.set('J');
        int e = rotorone.convertForward(0);
        assertEquals(e, 7);

        int y = rotorone.convertForward(25 - rotorone.setting());
        assertEquals(standard.toChar(y + rotorone.setting()), 'T');
    }

}



