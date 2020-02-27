package enigma;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Jerry
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;

        _permutation = _alphabet.toCharArray();
        _inversion = _alphabet.toCharArray();
        _cycles = cycles;

        /* split cycles into a list of small character cycle */
        String[] cycle = cycles.split("[\\t ()]");
        for(int i = 0 ; i < cycle.length; i++){
            addCycle(cycle[i]);
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        int cycleLen = cycle.length();
        /* alphabet cycle string's length =1 link to itself, not necessary to set*/
        if(cycleLen < 2){
            return;
        }
        /* from c0-cm initialized the _permutation and _inversion index char array */
        for(int i = 0 ; i < cycleLen; i++){
            /* forward index */
            int permIndex = (i+1) % cycleLen;
            /* backward index */
            int invertIndex = i>0 ? (i-1) % cycleLen : cycleLen-1;

            int currIndex = _alphabet.toInt(cycle.charAt(i));

            /* current char linked the correct character in forward cycle or backward cycle */
            _permutation[currIndex] = cycle.charAt(permIndex);
            _inversion[currIndex] = cycle.charAt(invertIndex);
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        int r = wrap(p);
        return _alphabet.toInt(_permutation[r]);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        int r = wrap(c);
        return _alphabet.toInt(_inversion[r]);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int r = wrap(_alphabet.toInt(p));
        return _permutation[r];
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int r = wrap(_alphabet.toInt(c));
        return _inversion[r];
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for(int i = 0 ; i < size(); i++){
            if(_inversion[i] == _alphabet.toChar(i)) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** My permutation string */
    private char[] _permutation;
    /** My inversion string, add this filed for performance considerations */
    private char[] _inversion;
    /** My cycles string */
    private String _cycles;
}
