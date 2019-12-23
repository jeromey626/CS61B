package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Jerome
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
            _input2 = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
            _input2 = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }

    }

    /** Return a Scanner reading from the file named NAME.
     * @param name2 */
    private Scanner getInput(String name2) {
        try {
            return new Scanner(new File(name2));
        } catch (IOException excp) {
            throw error("could not open %s", name2);
        }
    }

    /** Return a PrintStream writing to the file named NAME.
     * @param name1 */
    private PrintStream getOutput(String name1) {
        try {
            return new PrintStream(new File(name1));
        } catch (IOException excp) {
            throw error("could not open %s", name1);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine newM = readConfig();
        if (!_input.hasNext("[*]")) {
            throw new EnigmaException("wrong input format");
        }
        String nextL = _input.nextLine();
        while (_input.hasNext()) {
            String setting = nextL;
            if (!setting.contains("*")) {
                throw new EnigmaException("Wrong setting format");
            }
            setUp(newM, setting);
            nextL = (_input.nextLine()).toUpperCase();
            while (!nextL.contains("*")) {
                String outm = newM.convert(nextL.replaceAll(" ", ""));
                if (nextL.isEmpty()) {
                    _output.println();
                } else {
                    printMessageLine(outm);
                }
                if (!_input.hasNext()) {
                    nextL = "*";
                } else {
                    nextL = (_input.nextLine()).toUpperCase();
                }
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            if (!_config.hasNext()) {
                throw new EnigmaException("wrong config format");
            }
            String alpha = _config.nextLine();
            _alphabet = new Alphabet(alpha);
            if (!_config.hasNextInt()) {
                throw new EnigmaException("no rotor quantity");
            }
            int numRotors = _config.nextInt();
            if (!_config.hasNextInt()) {
                throw new EnigmaException("no pawl quantity");
            }
            int pawls = _config.nextInt();
            temp = (_config.next()).toUpperCase();
            while (_config.hasNext()) {
                name = temp;
                notches = (_config.next()).toUpperCase();
                _allRotors.add(readRotor());
            }
            return new Machine(_alphabet, numRotors, pawls, _allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            perm = "";
            temp = (_config.next().toUpperCase());
            while (_config.hasNext()) {
                if (temp.contains("(")) {
                    perm = perm.concat(temp + " ");
                    temp = (_config.next()).toUpperCase();
                } else {
                    break;
                }
            }

            if (notches.charAt(0) == 'M') {
                String notcheplaces = notches.substring(1);
                return new MovingRotor(name,
                        new Permutation(perm, _alphabet), notcheplaces);
            } else if (notches.charAt(0) == 'R') {
                return new Reflector(name, new Permutation(perm, _alphabet));
            } else {
                return new FixedRotor(name, new Permutation(perm, _alphabet));
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        Scanner reader = new Scanner(settings);
        String[] rotors = new String[M.numRotors()];
        String starting;
        String plugboard = "";
        if (!reader.hasNext("[*]")) {
            throw new EnigmaException("bad input format");
        }
        reader.next();
        for (int i = 0; i < M.numRotors(); i++) {
            rotors[i] = reader.next();
        }

        for (int i = 0; i < rotors.length - 1; i++) {
            for (int j = i + 1; j < rotors.length; j++) {
                if (rotors[i].equals(rotors[j])) {
                    throw new EnigmaException("Repeated Rotor");
                }
            }
        }
        M.insertRotors(rotors);
        if (!M.getRotorList()[0].reflecting()) {
            throw new EnigmaException("First Rotor should be a reflector");
        }
        starting = reader.next();
        M.setRotors(starting);
        if (starting.length() != rotors.length - 1) {
            throw new EnigmaException("settings string not the right length.");
        }

        String temp1;
        while (reader.hasNext()) {
            temp1 = reader.next();
            plugboard = plugboard.concat(temp1 + " ");
        }
        Permutation plug = new Permutation(plugboard, _alphabet);
        M.setPlugboard(plug);
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        for (int i = 0; i < msg.length(); i += 5) {
            int cap = msg.length() - i;
            if (cap <= 5) {
                _output.println(msg.substring(i, i + cap));
            } else {
                _output.print(msg.substring(i, i + 5) + " ");
            }
        }
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** copy int */
    private Scanner _input2;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** String for the perm cycle. */
    private  String perm;

    /** Temporary string. */
    private String temp;

    /** current rotor's name. */
    private String name;

    /** notches of current rotor. */
    private String notches;

    /** An ArrayList of ALL usable rotors. */
    private ArrayList<Rotor> _allRotors = new ArrayList<>();
}
