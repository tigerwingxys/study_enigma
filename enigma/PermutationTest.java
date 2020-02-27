package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;

import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/**
 * The suite of all JUnit tests for the Permutation class.
 *
 * @author Jerry
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
    public void checkRotorITransform() {
        perm = new Permutation("(AELTPHQXRU) (BKNW) (CMOY) (DFG) " +
                "(IV) (JZ) (S)", UPPER);
        char c = 'A';
        assertEquals(msg("I", "wrong permute of '%c'", c),
                'E', perm.permute(c));
        c = 'U';
        assertEquals(msg("I", "wrong permute of '%c'", c),
                'A', perm.permute(c));
        c = 'J';
        assertEquals(msg("I", "wrong permute of '%c'", c),
                'Z', perm.permute(c));
        c = 'Z';
        assertEquals(msg("I", "wrong permute of '%c'", c),
                'J', perm.permute(c));
        c = 'S';
        assertEquals(msg("I", "wrong permute of '%c'", c),
                'S', perm.permute(c));
        c = 'A';
        assertEquals(msg("I", "wrong invert of '%c'", c),
                'U', perm.invert(c));
        c = 'U';
        assertEquals(msg("I", "wrong invert of '%c'", c),
                'R', perm.invert(c));
        c = 'J';
        assertEquals(msg("I", "wrong invert of '%c'", c),
                'Z', perm.invert(c));
        c = 'Z';
        assertEquals(msg("I", "wrong invert of '%c'", c),
                'J', perm.invert(c));
        c = 'S';
        assertEquals(msg("I", "wrong invert of '%c'", c),
                'S', perm.invert(c));
    }

}
