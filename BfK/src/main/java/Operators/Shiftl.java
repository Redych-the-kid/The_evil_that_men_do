package Operators;

import Brainfuck.BFContext;
import Brainfuck.Operator;

/**
 * Shifts the data pointer to the left
 */
public class Shiftl implements Operator {
    public void do_stuff(BFContext ctx) {
        ctx.move_ptr(-1);
        ctx.inc();
    }
}
