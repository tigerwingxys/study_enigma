package enigma;

import java.util.Scanner;

/** Superclass that represents a rotor in the enigma machine.
 *  @author Jerry
 */
class Rotor {

    /** A rotor named NAME whose permutation is given by PERM. */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;

        _type = NOMOVING;
        _notches = "";
        _leftRotor = null;
        _position = 0;
        _ringOffset = 0;
        _hasAPawl = false;
    }

    static public Rotor makeARotor(Alphabet alphabet, String config){
        Scanner scanner = new Scanner(config);

        String name = scanner.next();
        String type = scanner.next();
        String notches = "";
        if( type.length() > 1){
            notches = type.substring(1);
        }
        String permute = scanner.nextLine();
        Permutation perm = new Permutation(permute, alphabet);

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
        return _type == MOVING;
    }

    /** Return true iff I reflect. */
    boolean reflecting() {
        return _type == REFLECTOR;
    }

    /** Return my current setting. */
    int setting() {
        return _position;
    }

    /** Set setting() to POSN.  */
    void set(int posn) {
        if(reflecting()){
            return;
        }
        _position = _permutation.wrap(posn);
    }

    /** Set setting() to character CPOSN. */
    void set(char cposn) {
        if(reflecting()){
            return;
        }
        if(permutation().alphabet().contains(cposn)) {
            _position = _permutation.alphabet().toInt(cposn);
        }else {
            _position = 0;
        }
    }

    /** Set ringing setting to NRINGPOSITION */
    void setRingPosition(int nringPosition){
        if(reflecting()){
            return;
        }
        _ringOffset = _permutation.wrap(nringPosition);
    }

    /** Set ringing setting to character CRINGPOSITION */
    void setRingPosition(char cringPostion){
        if(reflecting()){
            return;
        }
        if(permutation().alphabet().contains(cringPostion)) {
            _ringOffset = _permutation.alphabet().toInt(cringPostion);
        }else {
            _ringOffset = 0;
        }
    }

    /** Set this rotor a pawl */
    void setPawl(){
        if(!rotates()){
            return;
        }
        _hasAPawl = true;
    }

    /** Returns this rotor has a pawl*/
    boolean hasAPawl(){
        return _hasAPawl;
    }

    /** Set rotor's type to CTYPE */
    void setType(char ctype){
        if (ctype != REFLECTOR && ctype != NOMOVING && ctype != MOVING){
            throw new EnigmaException(String.format("Rotor type[%c] is not" +
                    " correct type",ctype));
        }
        _type = ctype;
    }

    /** Set notches of this rotor */
    void setNotches(String notches){
        if(!rotates()){
            return;
        }
        /* check if every char in notches in alphabet */
        for (char c : notches.toCharArray()) {
            if(!alphabet().contains(c)){
                throw new EnigmaException(String.format("Rotor notch[%c] is" +
                        " not in alphabet",c));
            }
        }
        _notches = notches;
    }

    /** Setup the left rotor */
    void setLeftRotor(Rotor leftRotor){
        _leftRotor = leftRotor;
    }

    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {
        int offset = _position + _ringOffset;
        return _permutation.wrap(_permutation.permute(p + offset) - offset);
    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation. */
    int convertBackward(int e) {
        int offset = _position + _ringOffset;
        return _permutation.wrap(_permutation.invert(e + offset) - offset);
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        int r = _permutation.wrap(_position + _ringOffset);
        char c =_permutation.alphabet().toChar(r);

        /* check the current position linked character in the notches */
        return _notches.indexOf(c) >= 0;
    }

    /** Advance me one position, if possible. By default, does nothing. */
    void advance() {
        if(!rotates()){
            return;
        }

        boolean leftHadAdvance = false;
        /* if this rotor at notch and has a left moving rotor, the left rotor
        advance a step */
        if(atNotch() && _leftRotor != null){
            _leftRotor.advance();
            leftHadAdvance = true;
        }

        /* current position advance a step */
        _position++;
        if(_position == size()){
            _position = 0;
        }

        /* if this rotor is not at notch, and the left rotor is at notch
           (not just advance here), and the left of the left rotor has a
           pawl, double stepping */
        if(!atNotch() && !leftHadAdvance && _leftRotor != null
                && _leftRotor.atNotch() && _leftRotor._leftRotor.hasAPawl())
        {
            _leftRotor.advance();
        }
    }

    /** return my type */
    char type(){
        return _type;
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /** The types of rotor: R - reflector, N - no moving, M - moving */
    static final char REFLECTOR = 'R';
    static final char NOMOVING = 'N';
    static final char MOVING = 'M';

    /** My name. */
    private final String _name;

    /** My permutation implemented by this rotor in its 0 position. */
    private Permutation _permutation;

    /** My position of this rotor */
    private int _position ;

    /** My ringOffset, meaning Ringstellung */
    private int _ringOffset;

    /** My type */
    private char _type;

    /** My notches */
    private String _notches;

    /** My pawl */
    private boolean _hasAPawl;

    /** My left MOVING rotor of this rotor */
    private Rotor _leftRotor;
}
