package com.redych.snakejfx.components;

import java.util.ArrayList;

public class Snake {
    public ArrayList<Bodyblock> blocks = new ArrayList<>();
    private Bodyblock head;
    private Bodyblock tail;
    private Field f;
    public Snake(int len, Field field){
        int ipx = field.get_width() / 2;
        int ipy = field.get_height() / 2;
        head = new Bodyblock(ipx, ipy, null, field);
        blocks.add(head);
        tail = head;
        for(int i = 1;i < len;++i){
            Bodyblock block = new Bodyblock(ipx + i, ipy, tail, field);
            blocks.add(block);
            tail = block;
        }
        f = field;
    }
    public void set_direction(DIRECTION d){
        head.set_direction(d);
    }
    public void grow(){
        Bodyblock b = new Bodyblock(get_tail().get_ox(), get_tail().get_oy(), get_tail(), f);
        tail = b;
        blocks.add(b);
        f.add_bodyblock(b);
    }
    public boolean is_dead(){
        for(Bodyblock b:blocks){
            if(b != head){
                if(b.get_x() == get_head().get_x() && b.get_y() == get_head().get_y()){
                    return true;
                }
            }
        }
        return f.is_obstacle(get_head().get_x(), get_head().get_y());
    }
    public DIRECTION get_direction(){
        return head.get_direction();
    }
    public Bodyblock get_head(){
        return head;
    }
    public Bodyblock get_tail(){
        return tail;
    }
    public void set_tail(Bodyblock b){
        tail = b;
    }
}
