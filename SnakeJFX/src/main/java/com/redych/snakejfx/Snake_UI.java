package com.redych.snakejfx;

import com.redych.snakejfx.components.DIRECTION;
import com.redych.snakejfx.components.Field;
import com.redych.snakejfx.components.Score;
import com.redych.snakejfx.components.Snake;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Snake_UI extends Application {
    private static final int cell_size = 30;
    private final int width = 30;
    private final int height = 15;
    private final int snake_len = 5;
    private long then = System.nanoTime();
    private boolean changed = false;
    private boolean has_next = false;
    private DIRECTION next_update;
    private Field field;
    private int speed = 8;
    private boolean is_scored = false;
    private boolean is_paused = false;
    private final String[] levels = {"test.txt", "empty.txt", "box.txt"};
    private final String[] level_names = {"Farm", "Freedom", "Box"};
    private int level = 0;
    @Override
    public void start(Stage stage) {
        VBox main = new VBox(10);
        VBox menu = new VBox(10);
        Scene menu_scene = new Scene(menu, 300, 250);

        main.setBackground((new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY))));
        main.setPadding(new Insets(10));
        Score score_manager = new Score();
        field = new Field(width, height, score_manager);
        field.add_snake(new Snake(snake_len, field));
        score_manager.load_save();
        Label high_score_label = new Label("High Score: " + score_manager.get_high_score());
        Label score_label = new Label("Score: 0");
        Label pause_label = new Label("PAUSED!");
        pause_label.setVisible(false);
        pause_label.setTextFill(Color.WHITE);
        score_label.setTextFill(Color.WHITE);
        high_score_label.setTextFill(Color.WHITE);
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(now - then > 1000000000 / speed) {
                    field.update();
                    then = now;
                    score_label.setText("Score: " + score_manager.get_score());
                    changed = false;
                    if(score_manager.get_score() > score_manager.get_high_score()){
                        score_manager.set_high_score();
                        high_score_label.setText("High Score: " + score_manager.get_high_score());
                        is_scored = true;
                    }
                    if(has_next){
                        set_direction(field.get_snake(), next_update);
                        has_next = false;
                    }
                    if(field.get_snake().is_dead()){
                        field.get_snake().grow();
                        stop();
                        Alert al = new Alert(Alert.AlertType.INFORMATION);
                        al.setHeaderText("Game over!");
                        if(is_scored){
                            al.setContentText("New record!Your score is:" + score_manager.get_score());
                        }
                        else{
                            al.setContentText("Your score is:" + score_manager.get_score());
                        }
                        Platform.runLater(al::showAndWait);
                        al.setOnHidden(event -> {
                            score_manager.save_data();
                            main.getChildren().clear();
                            score_manager.clear();
                            field = new Field(width, height, score_manager);
                            field.add_snake(new Snake(snake_len, field));
                            field.field_init(levels[level]);
                            score_label.setText("Score: 0");
                            main.getChildren().addAll(pause_label, field, score_label, high_score_label);
                            is_scored = false;
                            start();
                        });
                    }
                }
            }
        };
        main.getChildren().addAll(pause_label, field, score_label, high_score_label);
        Scene scene = new Scene(main);
        scene.setOnKeyPressed(e->{
            if(e.getCode().equals(KeyCode.W) && field.get_snake().get_direction() != DIRECTION.DOWN){
                set_direction(field.get_snake(), DIRECTION.UP);
            }
            if(e.getCode().equals(KeyCode.S) && field.get_snake().get_direction() != DIRECTION.UP){
                set_direction(field.get_snake(), DIRECTION.DOWN);
            }
            if(e.getCode().equals(KeyCode.A) && field.get_snake().get_direction() != DIRECTION.RIGHT){
                set_direction(field.get_snake(), DIRECTION.LEFT);
            }
            if(e.getCode().equals(KeyCode.D) && field.get_snake().get_direction() != DIRECTION.LEFT){
                set_direction(field.get_snake(), DIRECTION.RIGHT);
            }
            if(e.getCode().equals(KeyCode.ESCAPE) && !is_paused){
                pause_label.setVisible(true);
                timer.stop();
                is_paused = true;
            }
            else if(e.getCode().equals(KeyCode.ESCAPE)){
                pause_label.setVisible(false);
                timer.start();
                is_paused = false;
            }
        });

        Button btn = new Button();
        btn.setText("Play");
        btn.setOnAction(e -> {
            timer.start();
            field.field_init(levels[level]);
            stage.setScene(scene);
        });

        Button btn2 = new Button();
        btn2.setText("Game speed:" + speed);
        btn2.setOnAction(e -> {
            if(speed < 10){
                speed++;
            }
            else{
                speed = 1;
            }
            btn2.setText("Game speed:" + speed);
        });
        Button btn3 = new Button();
        btn3.setText("Level:" + level_names[level]);
        btn3.setOnAction(e ->{
            if(level < 2){
                level++;
            }
            else{
                level = 0;
            }
            btn3.setText("Level:" + level_names[level]);
        });
        menu.setAlignment(Pos.BASELINE_CENTER);
        menu.getChildren().addAll(btn, btn2, btn3);
        stage.setResizable(false);
        stage.setScene(menu_scene);
        stage.setTitle("Snake 2 ripoff");
        stage.show();
    }

    public void set_direction(Snake s, DIRECTION d){
        if(!changed){
            s.set_direction(d);
            changed = true;
        }
        else{
            has_next = true;
            next_update = d;
        }
    }
    public static int get_cell_size(){return cell_size;}
    public static void main(String[] args) {
        launch();
    }
}