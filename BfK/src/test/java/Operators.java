import Brainfuck.Factory;
import Brainfuck.Runner;
import junit.framework.TestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Operators extends TestCase {
    private Runner runner;

    @AfterEach
    protected void shutdown(){
        runner = null;
    }
    @BeforeEach
    protected void setup() {
        Factory factory = Factory.Get();
        factory.clear();
    }

    @Test
    void basic(){
        runner = new Runner("basics");
        assertDoesNotThrow(() -> runner.parse());
        assertEquals(runner.get_context().get_data_pointer(), 0);
        assertEquals(runner.get_context().output(), (byte)255);
    }

    @Test
    void looping(){
        runner = new Runner("test_loop");
        assertDoesNotThrow(() -> runner.parse());
        assertEquals(runner.get_context().get_data_pointer(), 0);
        assertEquals(runner.get_context().output(), 0);
        runner.get_context().move_ptr(1);
        assertEquals(runner.get_context().get_data_pointer(), 1);
        assertEquals(runner.get_context().output(), 9);

    }

    @Test
    void looping_limit_left(){
        assertThrows(IllegalArgumentException.class, ()->new Runner("looping_limit_left"));
    }

    @Test
    void looping_limit_right(){
        assertThrows(IllegalArgumentException.class, ()->new Runner("looping_limit_right"));
    }

    @Test
    void nested() {
        runner = new Runner("nested_loop");
        // result: 3 * 5 * 3 = 45 in [2]
        assertDoesNotThrow(() -> runner.parse());
        assertEquals(runner.get_context().get_data_pointer(), 0);
        assertEquals(runner.get_context().output(), 0);
        runner.get_context().move_ptr(1);
        assertEquals(runner.get_context().get_data_pointer(), 1);
        assertEquals(runner.get_context().output(), 0);
        runner.get_context().move_ptr(1);
        assertEquals(runner.get_context().output(), 45);
    }
}
