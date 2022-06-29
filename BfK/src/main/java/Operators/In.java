package Operators;

import Brainfuck.BFContext;
import Brainfuck.Operator;

import java.util.Scanner;

public class In implements Operator {
    public void do_stuff(BFContext ctx){
        Scanner scanner = new Scanner(System.in);
        String symbols;
        while(true){
            symbols = scanner.next();
            if(symbols.length() != 1){
                System.out.println("Enter a SINGLE symbol");
            } else{
                break;
            }
        }
        char symbol = symbols.charAt(0);
        ctx.input((byte)symbol);
        ctx.inc();
    }
}
