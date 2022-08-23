package Brainfuck;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Runner class of brainfuck interpreter
 */
public class Runner {
    /**
     * Runner class constructor
     * @param path File path for a brainfuck program
     */
    public Runner(String path) throws IllegalArgumentException {
        String program = null;
        try{
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(path);
            if(in == null){
                throw new IOException("getResources failure for " + path);
            }
            program = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (IOException what){
            logger.error(what);
        }
        context = new BFContext(program);
        logger.info("Program loading success!");
    }

    /**
     * Runs brainfuck code loaded by constructor
     * @throws IllegalArgumentException - just 4 test)
     */
    public void parse() throws IllegalArgumentException{
        logger.info("Execution starts!");
        while(!context.is_finished()){
            Character curr = context.get_op_code();
            Factory.Get().get_op(curr).do_stuff(context);
        }
        System.out.println();
        logger.info("Execution is finished!");
    }
    /**
     * Context getter(for testing purposes)
     */
    public BFContext get_context(){
        return context;
    }
    private static final Logger logger = LogManager.getLogger(Runner.class);
    private final BFContext context;
}
