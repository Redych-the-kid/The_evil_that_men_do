package Brainfuck;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;

/**
 * BFContext - Context for brainfuck interpreter
 * It contains program string, code and op pointers, data array and methods
 * to interact with them.
 */
public class BFContext {
    /**
     * Constructor for BFContext.
     * @param code Code string
     * @throws IllegalArgumentException
     */
    public BFContext(String code) throws IllegalArgumentException {
        program = code;
        brackets_check();
        logger.info("Brackets check success!");
        data = new ArrayList<>(LIMIT);
        data.ensureCapacity(LIMIT);
        data.addAll(Collections.nCopies(LIMIT, (byte)0));
        data.trimToSize();
    }

    public void brackets_check() throws IllegalArgumentException{
        int counter = 0;
        for(int i = 0;i < program.length();++i){
            if(program.charAt(i) == '['){
                counter++;
            }
            if(program.charAt(i) == ']'){
                counter--;
            }
            if(counter < 0){
                logger.error("Extra closing bracket was found in your code!");
                throw new IllegalArgumentException();
            }
        }
        if(counter > 0){
            logger.error("Extra opening bracket was found in your code!");
            throw new IllegalArgumentException();
        }
    }
    /**
     * Moves data pointer
     * @param i Number of cells to move
     */
    public void move_ptr(int i){
        data_pointer += i;
    }

    /**
     * Increments operator pointer
     */
    public void inc(){
        operator_pointer++;
    }

    /**
     * Decrements operator pointer
     */
    public void decr(){
        operator_pointer--;
    }

    /**
     * Sets byte by given input
     * @param i - Byte input
     */
    public void input(byte i){
        data.set(data_pointer, i);
    }

    /**
     *  Gets current byte
      * @return Returns byte by current data_pointer
     */
    public byte output(){
        return data.get(data_pointer);
    }

    /**
     * Gets current op code
      * @return Returns current op code
     */
    public Character get_op_code(){
        return program.charAt(operator_pointer);
    }

    /**
     * Check if program is still running
     * @return Returns true if program is finished.Returns false otherwise.
     */
    public boolean is_finished(){
        return operator_pointer >= program.length();
    }
    private int data_pointer = 0;
    public int get_data_pointer(){
        return data_pointer;
    }
    private int operator_pointer = 0;
    private ArrayList<Byte> data;
    private final int LIMIT = 30000;
    private String program = null;
    public void set_program(String code) {
        program = code;
    }
    private static final Logger logger = LogManager.getLogger(BFContext.class);
}
