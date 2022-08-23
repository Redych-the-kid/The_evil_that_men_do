package Operators;

import Brainfuck.BFContext;
import Brainfuck.Operator;

/**
 * Decrements the byte that is currently pointed by data pointer
 */
public class Sub implements Operator {
    public void do_stuff(BFContext ctx){
        ctx.input((byte) (ctx.output() - 1));
        ctx.inc();
    }
}
