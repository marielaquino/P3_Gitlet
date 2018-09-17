package gitlet;

import ucb.junit.textui;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Mariel Aquino
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest() {
    }

    /** PRINTING / ERROR MESSAGE TESTS */

    /** Test printing of log command. */
    @Test
    public void logTest() {


    }

    /** Test printing of global log command. */
    @Test
    public void globalLogTest() {

    }

    /** Test init command correctly sends error message
     * if a directory already exists.
     */
    @Test
    public void initExistsTest() {

    }

    /** Init command test */
    @Test
    public void initTest() throws IOException {
        Commands.initCommand();
    }

    /** Test a faulty command call in the terminal. */
    @Test
    public void fakeCommandTest() {

    }

    @Test
    public void printTest() {
        System.out.println("<<<<<<< HEAD\n"
                + "hi" + "\n"
                + "=======\n"
                + "hi" + "\n"
                + ">>>>>>>");
    }


}


