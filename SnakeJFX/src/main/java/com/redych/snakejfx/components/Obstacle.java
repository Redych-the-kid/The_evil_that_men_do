package com.redych.snakejfx.components;

import com.redych.snakejfx.Snake_UI;
import javafx.scene.shape.Rectangle;

public class Obstacle extends Rectangle {
    private int pos_x;
    private int pos_y;
    public Obstacle(int x, int y){
        super(Snake_UI.get_cell_size(), Snake_UI.get_cell_size());
        pos_x = x;
        pos_y = y;
        setTranslateX(pos_x * Snake_UI.get_cell_size());
        setTranslateY((pos_y - 1) * Snake_UI.get_cell_size());
    }
    public int get_x(){
        return pos_x;
    }
    public int get_y(){
        return pos_y;
    }
}
