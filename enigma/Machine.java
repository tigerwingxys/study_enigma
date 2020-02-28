package enigma;

import javax.crypto.Mac;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.Scanner;

/** Class that represents a complete enigma machine.
 *  @author Jerry
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors)
    {
        _alphabet = alpha;

        _numRotors = numRotors;
        _numPawls = pawls;
        _allRotors = new HashMap<>();
        _plugboard = null;
        _atWorkRotors = new ArrayList<>();
        if(allRotors != null) {
            for (Rotor rotor : allRotors) {
                _allRotors.put(rotor.name(), rotor);
            }
        }
    }

    /** Returns a Machine according to default CONFIGLINES to initialize.
     *  @param configLines is a list of string lines, such as:
     *    list[0]: ABCDEFGHIJKLMNOPQRSTUVWXYZ
     *    list[1]: 5 3
     *    list[2]: I MQ      (AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)
     *    list[n]: ... ...
     *  from 2-n, every line is complete config to initialize a rotor. */
    public static Machine makeAMachine(ArrayList<String> configLines){
        /* initialize alphabet */
        Alphabet alphabet = new Alphabet(configLines.get(0));

        /* initialize the number of rotors and pawls */
        Scanner scanner = new Scanner(configLines.get(1));
        int numRotors = scanner.nextInt();
        int numPawls = scanner.nextInt();
        if(numPawls > numRotors - 1){
            throw new EnigmaException(String.format("numPawls[%d] is too" +
                    " big than numRotors[%d]", numPawls, numRotors));
        }

        /* initialize all supported rotors */
        ArrayList<Rotor> allRotors = new ArrayList<>();
        for(int i = 2; i < configLines.size(); i++){
            allRotors.add(Rotor.makeARotor(alphabet, configLines.get(i)));
        }

        return new Machine(alphabet, numRotors, numPawls, allRotors);
    }

    /** Setup the machine to correct rotors at work according CONFIGSTRING,
     *  positions, ringpositions(Ringstellung), plugboard.
     *  @param configString:* B Beta I II III AAAR BDFB (AQ) (EP)
     *    * indicate this is a config line
     *    B indicate the reflector
     *    Beta - III, is the rotors at work
     *    AAAR indicate the Beta - III's position
     *    BDFB indicate the Beta - III's Ringstellung settings
     *    (AQ) (EP) ..., the plugboard*/
    void setupMachine(String configString){
        _configString = configString.trim();

        String sRotor = _configString.substring(1).trim();
        String sPlugboard = "";
        int idx = _configString.indexOf('(');
        if(idx > 0){
            sRotor = _configString.substring(1,idx).trim();
            sPlugboard = _configString.substring(idx).trim();
        }
        String[] sNames = sRotor.split("[ \\t]");
        insertRotors(sNames);
        Permutation perm = new Permutation(sPlugboard, _alphabet);
        setPlugboard(perm);
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _numPawls;
    }

    /** Returns the current state of rotors position (except reflector) */
    String currentState(){
        StringBuilder result = new StringBuilder();
        for (int i = 1; i < numRotors(); i++) {
            result.append(_alphabet.toChar(_atWorkRotors.get(i).setting()));
        }
        return result.toString();
    }

    /** Returns the current ring positions of rotors (except reflector) */
    String currentRingPositions(){
        return _ringPositions;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        /* ignore the empty string in rotorsList */
        ArrayList<String> slRotors = new ArrayList<>();
        for (String s : rotors) {
            if (s.isEmpty()) {
                continue;
            }
            slRotors.add(s);
        }

        /* check number of rotors enough */
        if(slRotors.size() < _numRotors){
            throw new EnigmaException(String.format("This machine has %d " +
                    "rotors, but only give %d.",_numRotors, slRotors.size()));
        }

        /* set the rotor in order. */
        _atWorkRotors.clear();
        int i = 0;
        Rotor prevRotor = null;
        for(; i < _numRotors; i++){
            Rotor rotor = _allRotors.get(slRotors.get(i));
            if(rotor == null){
                throw new EnigmaException(String.format("This rotor[%s] " +
                        "is not support.",slRotors.get(i)));
            }
            if(i==0 && !rotor.reflecting()){
                throw new EnigmaException(String.format("This first rotor[%s]" +
                        " is not reflect type.",slRotors.get(i)));
            }
            _atWorkRotors.add(rotor);

            /* if has a pawl, then setup the left rotor */
            if(i >= _numRotors-_numPawls){
                rotor.setLeftRotor(prevRotor);
                rotor.setPawl();
            }
            prevRotor = rotor;
        }

        /* set the positions except the reflector */
        if(i < slRotors.size()){
            setRotors(slRotors.get(i));
        }

        /* optionally, set the ringpositions(Ringstellung) */
        if(i + 1 < slRotors.size()){
            setRingPositions(slRotors.get(i + 1));
        }
    }

    /** Set my rotors according to RINGPOSITIONS, which must be a string of
     * numRotor()-1 characters in my alphabet. The first letter refers to
     * the leftmost rotor ringPosition(not counting the reflector). */
    void setRingPositions(String ringPositions){
        if(ringPositions.length() < _numRotors - 1){
            throw new EnigmaException(String.format("This machine need " +
                    "set %d rotors ringposition, but given %s", _numRotors - 1,
                    ringPositions));
        }
        _ringPositions = ringPositions;
        for (int i = 0; i < ringPositions.length() ; i++) {
            _atWorkRotors.get(i + 1).setRingPosition(ringPositions.charAt(i));
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if(setting.length() < _numRotors - 1){
            throw new EnigmaException(String.format("This machine need set " +
                    "%d rotors, but given %s", _numRotors - 1, setting));
        }
        for (int i = 0; i < setting.length(); i++) {
            _atWorkRotors.get(i + 1).set(setting.charAt(i));
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
        if(_atWorkRotors.size() != _numRotors){
            throw new EnigmaException("This machine has not any rotors in.");
        }
        if(!_alphabet.contains(_alphabet.toChar(c))){
            throw new EnigmaException("" + c + " not in the alphabet.");
        }
        /* first through plugboard */
        int r = _plugboard.permute(c);

        /* then, the rightmost rotor advance */
        _atWorkRotors.get(_numRotors - 1).advance();

        /* forward permutation from right to left */
        for (int i = _numRotors - 1 ; i > 0 ; i--) {
            r = _atWorkRotors.get(i).convertForward(r);
        }

        /* reflect */
        r = _atWorkRotors.get(0).convertForward(r);

        /* backward inversion from left to right */
        for(int i = 1 ; i < _numRotors; i++){
            r = _atWorkRotors.get(i).convertBackward(r);
        }

        /* finally through plugboard */
        r = _plugboard.invert(r);

        return r;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        StringBuilder result = new StringBuilder();
        for (char c : msg.toCharArray()) {
            if (!_alphabet.contains(c)) {
                continue;
            }
            int r = _alphabet.toInt(c);
            r = convert(r);
            result.append(_alphabet.toChar(r));
        }
        return result.toString();
    }

    /** Common alphabet of my rotors. */
    private  Alphabet _alphabet;

    /** store all supported rotors */
    private HashMap<String, Rotor> _allRotors;

    /** This machine has total rotors */
    private int _numRotors;

    /** This machine has rotors(<=_numRotors-1) with pawls*/
    private int _numPawls;

    /** This machine's config string, such as "* B Beta I II III AAAR"*/
    private String _configString;

    /** The ringPositions of rotors at work (except reflector) */
    private String _ringPositions;

    /** This machine's plugboard */
    private Permutation _plugboard;

    /** The rotors list in order according to config string.
     * the first must be a reflector */
    private ArrayList<Rotor> _atWorkRotors;
}
