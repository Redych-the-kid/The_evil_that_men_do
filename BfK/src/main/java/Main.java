import Brainfuck.Runner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args){
        if(args.length != 1){
            System.out.println("No filename has been provided!Rerun with the provided filename!");
            return;
        }
        try{
            Runner runner = new Runner(args[0]);
            runner.parse();
        }
        catch(Exception how){
            logger.error(how);
        }
    }
}
