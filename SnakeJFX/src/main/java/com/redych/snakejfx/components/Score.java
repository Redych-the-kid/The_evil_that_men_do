package com.redych.snakejfx.components;

import java.io.*;

public class Score {
    private int score = 0;
    private int high_score = 0;
    private void create_save(){
        try{
            File file = new File("Save");
            FileWriter output = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(output);
            writer.write("" + 0);
        }
        catch (Exception e ){
            e.printStackTrace();
        }
    }
    public void load_save(){
        try{
            File f = new File("Save");
            if(!f.isFile()){
                create_save();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            high_score = Integer.parseInt(reader.readLine());
            reader.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void save_data(){
        FileWriter output = null;
        try{
            File f = new File("Save");
            output = new FileWriter(f);
            BufferedWriter writer = new BufferedWriter(output);
            writer.write("" + high_score);
            writer.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    public int get_score(){
        return score;
    }
    public int get_high_score(){return high_score;}
    public void set_high_score(){high_score = score;}
    public void incr_score(int inc){ score += inc;}
    public void clear(){score = 0;}
}
