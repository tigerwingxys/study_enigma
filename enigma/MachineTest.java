package enigma;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Machine class.
 *  @author Jerry
 */
public class MachineTest {
    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Machine machine;
    private String alpha = UPPER_STRING;
    private void checkAdvance(Machine M, String expectedSettings){
        M.convert(0);
        assertEquals(msg("Machine advance"," wrong " +
                "settings[%s] after advance",M.currentState()),
                expectedSettings, M.currentState());
    }

    /* ***** TESTS ***** */

    @Test
    public void checkAdvance() {
        Machine machine = Machine.makeAMachine(NAVALDEFAULT);

        machine.setupMachine("* B Beta I II III AAAV AAAA (AQ) (EP)");
        checkAdvance(machine, "AABW");
        machine.setupMachine("* B Beta I II III AAAU AAAB (AQ) (EP)");
        checkAdvance(machine, "AABV");
        machine.setupMachine("* B Beta I II III AADV AAAA (AQ) (EP)");
        checkAdvance(machine, "AAEW");
        checkAdvance(machine, "ABFX");
        machine.setupMachine("* B Beta I II III AQDV AAAA (AQ) (EP)");
        checkAdvance(machine, "AQEW");
        checkAdvance(machine, "ARFX");
        checkAdvance(machine, "ARFY");
        machine.setupMachine("* B Beta I II III APDV AAAA (AQ) (EP)");
        checkAdvance(machine, "APEW");
        machine.setupMachine("* B Beta I II III AQFV AAAA (AQ) (EP)");
        checkAdvance(machine, "AQGW");
        machine.setupMachine("* B Beta I II III AQDV AAAA (AQ) (EP)");
        checkAdvance(machine, "AQEW");

        machine = Machine.makeAMachine(SIMPLE);
        machine.setupMachine("*B Beta I II III AAAA");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 19 ; i++) {
            machine.convert(0);
            result.append(machine.currentState());
            result.append(" ");
        }
        assertEquals("Simple advance is not correct",
                "AAAB AAAC AABA AABB AABC AACA ABAB ABAC ABBA ABBB" +
                        " ABBC ABCA ACAB ACAC ACBA ACBB ACBC ACCA AAAB ",
                result.toString());
    }
}
