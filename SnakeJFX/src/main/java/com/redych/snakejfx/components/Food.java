package com.redych.snakejfx.components;

import com.redych.snakejfx.Snake_UI;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Food extends Rectangle {
    private final int pos_x;
    private final int pos_y;
    public Food(int x, int y){
        super(Snake_UI.get_cell_size(), Snake_UI.get_cell_size());
        pos_x = x;
        pos_y = y;
        setTranslateX(pos_x * Snake_UI.get_cell_size());
        setTranslateY(pos_y * Snake_UI.get_cell_size());
        setFill(Color.LIGHTGREEN);
        setStroke(Color.BLACK);
    }
    public int get_x(){
        return pos_x;
    }
    public int get_y(){
        return pos_y;
    }
}
