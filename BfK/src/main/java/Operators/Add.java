package Operators;

import Brainfuck.BFContext;
import Brainfuck.Operator;

/**
* Increments the byte that is currently pointed by data pointer
 */
public class Add implements Operator {
    public void do_stuff(BFContext ctx){
        ctx.input((byte) (ctx.output() + (byte)1));
        ctx.inc();
    }
}
