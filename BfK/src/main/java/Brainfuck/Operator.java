package Brainfuck;

/**
 * Interface for a brainfuck operators
 */
public interface Operator {
    /**
     * Executes brainfuck operation
     * @param ctx Brainfuck context
     * @see BFContext
     */
    public void do_stuff(BFContext ctx);
}
