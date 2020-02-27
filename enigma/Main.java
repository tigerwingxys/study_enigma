package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Jerry
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
            throw error("Only 1, 2, or 3 command-line arguments " +
                    "allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        /* read configurations to set an Enigma machine */
        Machine machine = readConfig();

        /* process(encode/decode) very line, and print the msg */
        while (_input.hasNext()){
            String aLine = _input.nextLine();
            aLine = aLine.trim();

            /* read setting line from _input, and initialize the machine */
            if( aLine.startsWith("*")){
                machine.setupMachine(aLine);
            }else {
                /* convert this line and print */
                aLine = machine.convert(aLine);
                printMessageLine(aLine);
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String s = _config.nextLine();
            _alphabet = new Alphabet(s);

            int numRotors = _config.nextInt();
            int numPawls = _config.nextInt();
            /* ignore this line left content */
            _config.nextLine();

            /* process all input rotors config string, because of one config
            * string could split to two lines, add to arrLines. */
            ArrayList<Rotor> allRotors = new ArrayList<>();
            ArrayList<String> arrLines = new ArrayList<>();
            String prevLine = "";
            while (_config.hasNext()){
                s = _config.nextLine().trim();

                if( s.startsWith("(") ){/* prevline is not end */
                    prevLine += " " + s;
                }else {/* a new rotor config line */
                    if( !prevLine.isEmpty() ) {
                        arrLines.add(prevLine);
                    }
                    prevLine = s;
                }
            }
            if( !prevLine.isEmpty() ){
                arrLines.add(prevLine);
            }

            /* initialize every rotor according to config string */
            for(String line: arrLines){
                allRotors.add(readRotor(line));
            }

            /* initialize the machine */
            return new Machine(_alphabet, numRotors, numPawls, allRotors);

        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor(String line) {
        try {
            Scanner scanner = new Scanner(line);

            String name = scanner.next();
            String type = scanner.next();
            String notches = "";
            if( type.length() > 1){
                notches = type.substring(1);
            }
            String permute = scanner.nextLine();
            Permutation perm = new Permutation(permute, _alphabet);

            Rotor rotor;
            if( type.charAt(0) == Rotor.MOVING ){
                rotor = new MovingRotor(name, perm, notches);
            }else if(type.charAt(0) == Rotor.NOMOVING ){
                rotor = new FixedRotor(name, perm);
            }else if( type.charAt(0) == Rotor.REFLECTOR ){
                rotor = new Reflector(name, perm);
            }else {
                throw new EnigmaException(String.format("type[%c] is " +
                        "not correct.", type.charAt(0)));
            }
            return rotor;
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        int cnt = msg.length();
        int index = 0;
        while (index < cnt){
            if(index + 5 > cnt){
                _output.print(msg.substring(index));
                index = cnt;
            } else {
                _output.print(msg.substring(index, index + 5));
                index += 5;
            }
            if(index < cnt){
                _output.print(' ');
            }
        }
        _output.println();
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;
}
