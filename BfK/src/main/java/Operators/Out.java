package Operators;

import Brainfuck.BFContext;
import Brainfuck.Operator;

/**
 * Outputs a single char in stdout
 */
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
