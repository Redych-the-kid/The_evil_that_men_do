package com.redych.snakejfx.components;

import com.redych.snakejfx.Snake_UI;
import javafx.scene.shape.Rectangle;

public class Bodyblock extends Rectangle {
    private int pos_x;
    private int pos_y;
    private int old_pos_x;
    private int old_pos_y;
    private int max_x;
    private int max_y;
    private DIRECTION direction = DIRECTION.LEFT;
    private Bodyblock previous;
    public Bodyblock(int x,int y, Bodyblock p, Field f){
        super(Snake_UI.get_cell_size(), Snake_UI.get_cell_size());
        pos_x = x;
        pos_y = y;
        setTranslateX(pos_x * Snake_UI.get_cell_size());
        setTranslateY(pos_y * Snake_UI.get_cell_size());
        previous = p;
        max_x = f.get_width();
        max_y = f.get_height();
    }
    public void update(){
        old_pos_x = pos_x;
        old_pos_y = pos_y;
        if(previous == null){
            switch (direction){
                case UP -> move_up();
                case DOWN -> move_down();
                case LEFT -> move_left();
                case RIGHT -> move_right();
            }
        }
        else{
            pos_x = previous.old_pos_x;
            pos_y = previous.old_pos_y;
        }
        update_pos();
    }

    private void move_down() {
        pos_y++;
        if(pos_y >= max_y){
            pos_y = 0;
        }
    }
    private void move_left(){
        pos_x--;
        if(pos_x < 0){
            pos_x = max_x - 1;
        }
    }
    private void move_right(){
        pos_x++;
        if(pos_x >= max_x){
            pos_x = 0;
        }
    }
    private void move_up() {
        pos_y--;
        if(pos_y < 0){
            pos_y = max_y - 1;
        }
    }
    public void update_pos(){
        setTranslateX(pos_x * Snake_UI.get_cell_size());
        setTranslateY(pos_y * Snake_UI.get_cell_size());
    }

    public void set_direction(DIRECTION d) {
        direction = d;
    }
    public DIRECTION get_direction(){
        return direction;
    }
    public int get_x(){
        return pos_x;
    }
    public int get_y(){
        return pos_y;
    }
    public int get_ox(){
        return old_pos_x;
    }
    public int get_oy(){
        return old_pos_y;
    }
}
