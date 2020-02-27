package enigma;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Jerry
 */
class Alphabet {

    /** A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        if (chars == null || chars.length() == 0){
            throw new EnigmaException("Alphabet input param[chars] is null or empty");
        }
        char begin = chars.charAt(0);
        for(int i = 0 ; i < chars.length(); i++){
            if((chars.charAt(i) - begin) != i){
                throw new EnigmaException(String.format("Alphabet char[%c]-[%c] is not [%d]",
                        chars.charAt(i), begin, i));
            }
        }
        _alphabet = chars;
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _alphabet.length();
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        return _alphabet.charAt(0) <= ch && ch <= _alphabet.charAt(size()-1);
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        if(index < 0 || index >= size()){
            throw new EnigmaException("index must between 0 - size-1.");
        }
        return _alphabet.charAt(index);
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        if(!contains(ch)){
            throw new EnigmaException("Alphabet not contains this character[" + ch + "].");
        }
        return ch - _alphabet.charAt(0);
    }

    /** Returns the char array of Alphabet */
    char[] toCharArray(){
        return _alphabet.toCharArray();
    }

    /** the field of the alphabet */
    private String _alphabet;
}
