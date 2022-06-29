package Operators;

import Brainfuck.BFContext;
import Brainfuck.Operator;

public class Out implements Operator {
    public void do_stuff(BFContext ctx) {
        if(ctx.output() == (byte)10){
            System.out.println();
        }
        else{
            System.out.print((char)ctx.output());
        }
        ctx.inc();
    }
}
