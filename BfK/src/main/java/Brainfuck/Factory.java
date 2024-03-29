package Brainfuck;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Properties;

/**
 * Factory for brainfuck operators
 */
public class Factory {
    /**
     * Getter for Factory
     * @return Returns Factory instance
     */
    public static Factory Get(){
        if(null == instance){
            instance = new Factory();
        }
        return instance;
    }

    /**
     * For test purposes...
     */
    public void clear(){
        missing_ops.clear();
        ops.clear();
        props.clear();
        read_file();
    }
    /**
     * Gets operator from its char code
     * @param code Character code for operator
     * @return returns Operator instance
     */
    public Operator get_op(char code){
        Operator op = ops.get(code);
        if(null == op){
            op = new_op(code);
        }
        return op;
    }

    private Factory(){
        ops = new HashMap<>(100);
        props = new Properties();
        missing_ops = new HashMap<>(100);
        read_file();
    }
    private void read_file(){
        try{
            InputStream str = this.getClass().getClassLoader().getResourceAsStream(Factory.path);
            if(null == str){
                throw new IOException("getResource failed while opening" + path);
            }
            props.load(str);
        }
        catch(IOException what){
            logger.error(what);
        }
    }
    private Operator new_op(char code){
        if(missing_ops.containsKey(code)){
            return null;
        }
        String name = props.getProperty(Character.toString(code));
        if(name == null){
            missing_ops.put(code, true);
            return null;
        }
        Class<?> new_class;
        Constructor<?> constructor;
        try{
            new_class = Class.forName(name);
            constructor = new_class.getConstructor();
        }
        catch(ClassNotFoundException what){
            missing_ops.put(code,true);
            logger.error("Error!Class named " + name + " not found!");
            return null;
        }
        catch(NoSuchMethodException how){
            missing_ops.put(code,true);
            logger.error("Error!Constructor was not found for a class named " + name);
            return null;
        }
        Operator op = null;
        try {
            op = (Operator) constructor.newInstance();
            ops.put(code, op);
            logger.info("Code (" + code + ") has been added to the operators");
        }
        catch(Exception why){
            logger.error("Error getting instance for operation named " + name);
            why.printStackTrace();
        }

        return op;
    }
    private static Factory instance = null;
    private static Properties props = null;
    private static HashMap<Character, Operator> ops = null;
    private static HashMap<Character, Boolean> missing_ops = null;
    private static final String path = "operators.cfg";
    private static final Logger logger = LogManager.getLogger(Factory.class);
}
