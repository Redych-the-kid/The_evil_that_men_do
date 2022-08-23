package Operators;

import Brainfuck.BFContext;
import Brainfuck.Operator;

import java.util.Scanner;

/**
 * Reads a single char input from stdin
 */
public class In implements Operator {
    public void do_stuff(BFContext ctx){
        Scanner scanner = new Scanner(System.in);
        ctx.input((byte)(scanner.next().charAt(0)));
        ctx.inc();
    }
}
