package Operators;

import Brainfuck.BFContext;
import Brainfuck.Operator;

public class Add implements Operator {
    public void do_stuff(BFContext ctx){
        ctx.input((byte) (ctx.output() + (byte)1));
        ctx.inc();
    }
}
