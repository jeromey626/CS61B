package enigma;
import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.*;


import static enigma.TestUtils.*;

public class MachineTest {
    @Test
    public void insertRotorsTest() {
        Alphabet standard = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Permutation test1 = new Permutation("(ABDEKLMNOP) (CFGHIJQRS) "
                + "(TUVWXYZ)", standard);
        Permutation test2 = new Permutation("(ABCDEFGIJKLMNOPQUTUVWYZ) "
                + "(H) (R) (X)", standard);
        Permutation test3 = new Permutation("(ABDEKLMNOP) (CFGH) (IJQRS) "
                + "(TUV) (WXYZ)", standard);

        Rotor rotor1 = new Reflector("BLOOD", test1);
        Rotor rotor2 = new Rotor("DANCE", test2);
        Rotor rotor3 = new MovingRotor("BOY", test3, "Q");
        Rotor rotor4 = new MovingRotor("LOVE", test1, "I");

        ArrayList<Rotor> rotorCollection = new ArrayList<>();
        rotorCollection.add(rotor1);
        rotorCollection.add(rotor2);
        rotorCollection.add(rotor3);
        rotorCollection.add(rotor4);

        Machine machine1 = new Machine(standard, 3, 2, rotorCollection);
        String[] inp = new String[] {"BLOOD", "DANCE", "BOY"};
        machine1.insertRotors(inp);
        assertEquals(machine1.getRotorList().length, 3);

        inp[2] = "LOVE SHOT";
        machine1.insertRotors(inp);
        assertEquals(machine1.getRotorList().length, 3);

    }

    @Test
    public void setTest() {
        Alphabet standard = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Permutation test1 = new Permutation("(ABDEKLMNOP) "
                + "(CFGHIJQRS) (TUVWXYZ)", standard);
        Permutation test2 = new Permutation("(ABCDEFGIJKLMNOPQUTUVWYZ) "
                + "(H) (R) (X)", standard);
        Permutation test3 = new Permutation("(ABDEKLMNOP) (CFGH) (IJQRS) "
                + "(TUV) (WXYZ)", standard);

        Rotor rotor1 = new Reflector("BLOOD", test1);
        Rotor rotor2 = new FixedRotor("DANCE", test2);
        Rotor rotor3 = new MovingRotor("BOY", test3, "Q");
        Rotor rotor4 = new MovingRotor("LOVE", test1, "I");

        ArrayList<Rotor> rotorCollection = new ArrayList<>();
        rotorCollection.add(rotor1);
        rotorCollection.add(rotor2);
        rotorCollection.add(rotor3);
        rotorCollection.add(rotor4);

        Machine machine1 = new Machine(standard, 3, 2, rotorCollection);
        String[] inp = new String[] {"BLOOD", "DANCE", "BOY"};
        machine1.insertRotors(inp);
        machine1.setRotors("BC");

        assertEquals(machine1.getRotorList()[1].setting(), 1);
        assertEquals(machine1.getRotorList()[2].setting(), 2);

    }

    @Test
    public void convertTest() {
        Alphabet standard = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Permutation perm1 = new Permutation("(AE) (BN) (CK) (DQ) "
                + "(FU) (GY) (HW) "
                + "(IJ) (LO) (MP) (RX) (SZ) (TV)", standard);
        Permutation perm2 = new Permutation("(ALBEVFCYODJWUGNMQTZSKPR) "
                + "(HIX)", standard);
        Permutation perm3 = new Permutation("(ABDHPEJT) (CFLVMZOYQIRWUKXSG) "
                + "(N)", standard);
        Permutation perm4 = new Permutation("(AEPLIYWCOXMRFZBSTGJQNH) "
                + "(DV) (KU)", standard);
        Permutation perm5 = new Permutation("(AELTPHQXRU) (BKNW) (CMOY) "
                + "(DFG) (IV) (JZ) (S)", standard);
        Permutation plug1 = new Permutation("(HQ) (EX) (IP) "
                + "(TR) (BY)", standard);

        Rotor rotor1 = new Reflector("YA", perm1);
        Rotor rotor2 = new FixedRotor("YE", perm2);
        Rotor rotor3 = new MovingRotor("YU", perm3, "V");
        Rotor rotor4 = new MovingRotor("YI", perm4, "J");
        Rotor rotor5 = new MovingRotor("YO", perm5, "Q");

        ArrayList<Rotor> rotorCollection = new ArrayList<>();
        rotorCollection.add(rotor1);
        rotorCollection.add(rotor2);
        rotorCollection.add(rotor3);
        rotorCollection.add(rotor4);
        rotorCollection.add(rotor5);

        Machine machine1 = new Machine(standard, 5, 3, rotorCollection);
        String[] inputs = new String[] {"YA", "YE", "YU", "YI", "YO"};
        machine1.insertRotors(inputs);
        machine1.setRotors("AXLE");
        machine1.setPlugboard(plug1);
        assertEquals(machine1.convert("FROMHISSHOULDERHIAWATHA"),
                "QVPQSOKOILPUBKJZPISFXDW");
        assertEquals(machine1.convert("TOOKTHECAMERAOFROSEWOOD"),
                "BHCNSCXNUOAATZXSRCFYDGU");
        assertEquals(machine1.convert("MADEOFSLIDINGFOLDINGROSEWOOD"),
                "FLPNXGXIXTYJUJRCAUGEUNCFMKUF");
    }

    @Test
    public void uniqueRotorTest() {
        Alphabet standard = new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Permutation test1 = new Permutation("(ABDEKLMNOP) (CFGHIJQRS) "
                + "(TUVWXYZ)", standard);
        Rotor rotor1 = new MovingRotor("T", test1, "");
        Rotor rotor2 = new Rotor("T", test1);

        ArrayList<Rotor> rotorCollection = new ArrayList<>();
        rotorCollection.add(rotor1);
        rotorCollection.add(rotor2);

        int succ = -1;
        try {
            Machine machine1 = new Machine(standard, 3, 2, rotorCollection);
            succ = 1;
        } catch (EnigmaException expected) {
            succ = 0;
        }
        assertEquals(succ, 0);

        Rotor rotor3 = new Rotor("H", test1);
        rotorCollection.set(1, rotor3);

        succ = -1;
        try {
            Machine machine1 = new Machine(standard, 3, 2, rotorCollection);
            succ = 1;
        } catch (EnigmaException expected) {
            succ = 0;
        }
        assertEquals(succ, 1);
    }
}
