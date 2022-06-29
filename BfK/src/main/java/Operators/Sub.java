package Operators;

import Brainfuck.BFContext;
import Brainfuck.Operator;

public class Sub implements Operator {
    public void do_stuff(BFContext ctx){
        ctx.input((byte) (ctx.output() - 1));
        ctx.inc();
    }
}
