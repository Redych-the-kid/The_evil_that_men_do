package Operators;

import Brainfuck.BFContext;
import Brainfuck.Operator;

public class Shiftl implements Operator {
    public void do_stuff(BFContext ctx) {
        ctx.move_ptr(-1);
        ctx.inc();
    }
}
