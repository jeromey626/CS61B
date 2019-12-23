package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Jerome Chen
 */
public class PermutationTest {

    /**
     * Testing time limit.
     */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /**
     * Check that perm has an alphabet whose size is that of
     * FROMALPHA and TOALPHA and that maps each character of
     * FROMALPHA to the corresponding character of FROMALPHA, and
     * vice-versa. TESTID is used in error messages.
     */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                    e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                    c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                    ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                    ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }

    @Test
    public void permuteInt() {
        Alphabet standard = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Permutation test1 = new Permutation("(ABDEKLMNOP) (CFGHIJQRS) "
                + "(TUVWXYZ)", standard);
        assertEquals(test1.permute(0), 1);
        assertEquals(test1.permute(1), 3);
        assertEquals(test1.permute(15), 0);
        assertEquals(test1.permute(25), 19);

        Permutation test2 = new Permutation("(BCDEFGHIJKLMNOQRSTUVWXY) (P) "
                + "(A) (Z)", standard);
        assertEquals(test2.permute(0), 0);
        assertEquals(test2.permute(15), 15);
        assertEquals(test2.permute(25), 25);
    }

    @Test
    public void permuteChar() {
        Alphabet standard = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Permutation test = new Permutation("(ABEKLMNOP) (FGHIJQRS) "
                + "(TUVWXYZ) (CD)", standard);
        assertEquals(test.permute('K'), 'L');
        assertEquals(test.permute('S'), 'F');
        assertEquals(test.permute('D'), 'C');
    }

    @Test
    public void invertInt() {
        Alphabet standard = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Permutation test = new Permutation("(ABDEKLMNOP) (CFGHIJQRS) (TUVWXYZ)",
                standard);
        assertEquals(test.invert(0), 15);
        assertEquals(test.invert(2), 18);
        assertEquals(test.invert(25), 24);
    }

    @Test
    public void derangementtest() {
        Alphabet standard = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Permutation test = new Permutation("(ABDEKLMNOP) (CFGHIJQRS) (TUVWXYZ)",
                standard);
        assertTrue(test.derangement());
        Permutation test2 = new Permutation("", standard);
        assertFalse(test2.derangement());
    }

    @Test
    public void invertChar() {
        Alphabet standard = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Permutation test = new Permutation("(ABDEKLMNOP) (CFGHIJQRS) (TUVWXYZ)",
                standard);
        assertEquals(test.invert('C'), 'S');
        assertEquals(test.invert('M'), 'L');
        assertEquals(test.invert('S'), 'R');
    }

    @Test
    public void testPermutations() {
        Alphabet alphabet = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Permutation testOne = new Permutation("(ABC) (DEFGHIJKLM) (N) "
                + "(OPQRSTU) (VW) (XYZ)", alphabet);
        assertEquals('C', testOne.permute('B'));
        assertEquals('A', testOne.permute('C'));
        assertEquals(13, testOne.permute(13));
        assertEquals(15, testOne.permute(14));
        assertEquals(1, testOne.permute(0));
        assertEquals('N', testOne.permute('N'));
        assertEquals('V', testOne.permute('W'));
        Permutation testTwo = new Permutation("(ZYX) (WVUT) (SRQPON) (MLK) "
                + "(JIHGFEDC) (B) (A)", alphabet);
        assertEquals('Y', testTwo.permute('Z'));
        assertEquals('B', testTwo.permute('B'));
        assertEquals('A', testTwo.permute('A'));
        assertEquals(0, testTwo.permute(0));
        assertEquals(24, testTwo.permute(25));
        Permutation testThree = new Permutation("(NEIM) (KQH) (A) (O) (RPL) "
                + "(JGYFTV) (UCB) (XWZDS)", alphabet);
        assertEquals('K', testThree.permute('H'));
        assertEquals('O', testThree.permute('O'));
        assertEquals(0, testThree.permute(0));
        assertEquals(20, testThree.permute(1));
        assertEquals('W', testThree.permute('X'));
        Permutation testFour = new Permutation("(QM) (JVZA) (PDOWCNUTIY) (S) "
                + "(K) (R) (EHG) (FBLX)", alphabet);
        assertEquals('K', testFour.permute('K'));
        assertEquals('F', testFour.permute('X'));
        assertEquals(10, testFour.permute(10));
        assertEquals(4, testFour.permute(6));
        assertEquals('Q', testFour.permute('M'));
        Permutation testFive = new Permutation("(ABC) (DEF) (G) "
                + "(H) (IJK)", alphabet);
        assertEquals('L', testFive.permute('L'));
        assertEquals(11, testFive.permute(11));
    }

    @Test
    public void testInvert() {
        Alphabet alphabet = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Permutation testOne = new Permutation("(ABC) (DEFGHIJKLM) (N) "
                + "(OPQRSTU) (VW) (XYZ)", alphabet);
        assertEquals('A', testOne.invert('B'));
        assertEquals('B', testOne.invert('C'));
        assertEquals(13, testOne.invert(13));
        assertEquals(14, testOne.invert(15));
        assertEquals(2, testOne.invert(0));
        assertEquals('N', testOne.invert('N'));
        assertEquals('V', testOne.invert('W'));
        Permutation testTwo = new Permutation("(ZYX) (WVUT) (SRQPON) (MLK) "
                + "(JIHGFEDC) (B) (A)", alphabet);
        assertEquals('X', testTwo.invert('Z'));
        assertEquals('B', testTwo.invert('B'));
        assertEquals('A', testTwo.invert('A'));
        assertEquals(0, testTwo.invert(0));
        assertEquals(1, testTwo.invert(1));
        assertEquals(25, testTwo.invert(24));
        Permutation testThree = new Permutation("(NEIM) (KQH) (A) (O) (RPL) "
                + "(JGYFTV) (UCB) (XWZDS)", alphabet);
        assertEquals('H', testThree.invert('K'));
        assertEquals('O', testThree.invert('O'));
        assertEquals(0, testThree.invert(0));
        assertEquals(1, testThree.invert(20));
        assertEquals('X', testThree.invert('W'));
        Permutation testFour = new Permutation("(QM) (JVZA) (PDOWCNUTIY) (S) "
                + "(K) (R) (EHG) (FBLX)", alphabet);
        assertEquals('K', testFour.invert('K'));
        assertEquals('X', testFour.invert('F'));
        assertEquals(10, testFour.invert(10));
        assertEquals(6, testFour.invert(4));
        assertEquals('M', testFour.invert('Q'));
        Permutation testFive = new Permutation("(ABC) (DEF) (G) (H) "
                + "(IJK)", alphabet);
        assertEquals('L', testFive.invert('L'));
        assertEquals(11, testFive.invert(11));
    }

}
