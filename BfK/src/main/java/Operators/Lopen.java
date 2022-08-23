package Operators;

import Brainfuck.BFContext;
import Brainfuck.Operator;

/**
 * Opening bracket of the loop
 */
public class Lopen implements Operator {
    public void do_stuff(BFContext ctx) {
        if(ctx.output() == 0){
            int counter = 1;
            while(counter != 0){
                ctx.inc();
                if(ctx.get_op_code() == '['){
                    counter++;
                }
                if(ctx.get_op_code() == ']'){
                    counter--;
                }
            }
        }
        ctx.inc();
    }
}
