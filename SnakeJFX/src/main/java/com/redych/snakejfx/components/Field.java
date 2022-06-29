package com.redych.snakejfx.components;

import com.redych.snakejfx.Snake_UI;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Field extends Pane {
    private final int width;
    private final int height;
    private Snake snake;
    private Food f;
    private int[][] file_field = null;
    private final Score score;
    public Field(int new_width, int new_height, Score score_m){
        width = new_width;
        height = new_height;
        score = score_m;
        setMinSize(width * Snake_UI.get_cell_size(), height * Snake_UI.get_cell_size() );
        setBackground(new Background(new BackgroundFill(Color.GREEN,null,null)));
    }
    public void add_snake(Snake s){
        snake = s;
        for(Bodyblock b:s.blocks){
            add_bodyblock(b);
        }
    }
    public void add_food(){
        int rng_x = (int) (Math.random() * width);
        int rng_y = (int) (Math.random() * height);
        boolean status = is_obstacle(rng_x, rng_y);
        if(status){
            while(status){
                rng_x = (int) (Math.random() * width);
                rng_y = (int) (Math.random() * height);
                status = is_obstacle(rng_x, rng_y);
            }
        }
        Food food = new Food(rng_x, rng_y);
        getChildren().add(food);
        if(f != null){
            getChildren().remove(f);
        }
        f = food;
    }
    public void generate_obstacles(String level){
        file_field = get_field("levels/" + level);
        for(int i = 1;i <= height;++i){
            for(int j = 0;j < width;++j){
                assert file_field != null;
                if(file_field[i - 1][j] == 1){
                    Obstacle obstacle = new Obstacle(j, i);
                    add_obstacle(obstacle);
                }
            }
        }
    }
    public void add_bodyblock(Bodyblock b){
        getChildren().add(b);
    }
    private void add_obstacle(Obstacle o){
        getChildren().add(o);
    }
    public int get_width(){
        return width;
    }
    public int get_height(){
        return height;
    }
    public Snake get_snake(){
        return snake;
    }
    public void update(){
        for(Bodyblock b:snake.blocks){
            b.update();
        }
        if (is_eaten(f)){
            score.incr_score(20);
            add_food();
            snake.grow();
        }
    }
    public boolean is_eaten(Food f){
        if(f == null){
            return false;
        }
        return f.get_x() == snake.get_head().get_x() && f.get_y() == snake.get_head().get_y();
    }

    public boolean is_obstacle(int x, int y){
        return file_field[y][x] == 1;
    }

    public void field_init(String level){
        generate_obstacles(level);
        add_food();
    }
    private int[][]get_field(String file_name){
        try{
            Scanner sc = new Scanner(new BufferedReader(new FileReader(file_name)));
            int rows = 15;
            int columns = 30;
            int [][] myArray = new int[rows][columns];
            while(sc.hasNextLine()) {
                for (int i=0; i<myArray.length; i++) {
                    String[] line = sc.nextLine().trim().split(" ");
                    for (int j=0; j<line.length; j++) {
                        myArray[i][j] = Integer.parseInt(line[j]);
                    }
                }
            }
            return myArray;
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }
}
