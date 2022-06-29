package Operators;

import Brainfuck.BFContext;
import Brainfuck.Operator;

public class Lclose implements Operator {
    public void do_stuff(BFContext ctx) {
        if(ctx.output() != 0){
            int counter = 1;
            while(counter != 0){
                ctx.decr();
                if(ctx.get_op_code() == '['){
                    counter--;
                }
                if(ctx.get_op_code() == ']'){
                    counter++;
                }
            }
        }
        ctx.inc();
    }
}
