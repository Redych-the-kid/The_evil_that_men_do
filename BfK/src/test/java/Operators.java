import Brainfuck.Factory;
import Brainfuck.Runner;
import junit.framework.TestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Operators extends TestCase {
    private Runner runner;
    private Factory factory;

    @AfterEach
    protected void shutdown(){
        runner = null;
    }
    @BeforeEach
    protected void setup() throws Exception{
        factory = Factory.Get();
        factory.clear();
    }

    @Test
    void basic(){
        String code = ">++<-";
        runner = new Runner("fib");
        runner.set_code(code);
        runner.parse();
        assertEquals(runner.get_context().get_data_pointer(), 0);
        assertEquals(runner.get_context().output(), (byte)255);
    }

    @Test
    void looping(){
        String code = "[++++";
        runner = new Runner("fib");
        runner.set_code(code);
        assertThrows(Exception.class, () -> runner.parse());
        code = "++++]";
        runner.set_code(code);
        assertThrows(Exception.class, () -> runner.parse());
        code = "+++[>+++<-]";
        runner.set_code(code);
        assertDoesNotThrow(() -> runner.parse());

        assertEquals(runner.get_context().get_data_pointer(), 0);
        assertEquals(runner.get_context().output(), 0);

        runner.get_context().move_ptr(1);
        assertEquals(runner.get_context().get_data_pointer(), 1);
        assertEquals(runner.get_context().output(), 9);
    }
}
