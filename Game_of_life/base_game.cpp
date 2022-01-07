#include "base_game.h"

std::pair<size_t, size_t> base_game::max_size = std::make_pair(500,500);
std::pair<size_t, size_t> base_game::min_size = std::make_pair(10,10);

base_game::base_game(std::pair<size_t, size_t> def_size) : size(def_size){
    b_rule = {0,0,0,1,0,0,0,0,0};
    s_rule = {0,0,1,1,0,0,0,0,0}; //b3s23
    game_field.resize(size.first * size.second);
    new_generation.resize(size.first * size.second);
}

void base_game::new_iteration(){
    new_generation.clear();
    new_generation.resize(size.first * size.second);
    for(size_t i = 0;i < size.second;++i){
        for(size_t j = 0;j < size.first;++j){
            new_generation[i * size.first + j] = survive(j, i);
        }
    }
    game_field = new_generation;
}

size_t base_game::survive(size_t x, size_t y)
{
    int neighbour_count = 0;
    size_t prev_x = (x > 0 ? x - 1 : size.first - 1);
    size_t prev_y = (y > 0 ? y - 1 : size.second - 1);
    size_t next_x = (x < size.first - 1 ? x + 1 : 0);
    size_t next_y = (y < size.second - 1 ? y + 1 : 0);
    neighbour_count += game_field[prev_y * size.first +  x];
    neighbour_count += game_field[next_y * size.first +  x];
    neighbour_count += game_field[y * size.first +  next_x];
    neighbour_count += game_field[y * size.first +  prev_x];
    neighbour_count += game_field[prev_y * size.first +  prev_x];
    neighbour_count += game_field[prev_y * size.first +  next_x];
    neighbour_count += game_field[next_y * size.first +  prev_x];
    neighbour_count += game_field[next_y * size.first +  next_x];
    if (game_field[y * size.first + x] == 0 && b_rule[neighbour_count] == 1)
           return true;
    if (game_field[y * size.first + x] == 1 && s_rule[neighbour_count] == 1)
           return true;
    return false;
}

void base_game::third_impact() //it all returns to nothing...
{
    game_field.clear();
    new_generation.clear();
    game_field.resize((size.first) * (size.second));
    new_generation.resize((size.first) * (size.second));
}

void base_game::resize(std::pair<size_t, size_t> new_size)
{
    size = new_size;
    third_impact();
}

void base_game::set_h(size_t height)
{
    size.second = height;
}

void base_game::set_w(size_t width)
{
    size.first = width;
}

void base_game::set_alive(size_t x, size_t y)
{
    game_field[y * size.first +  x] = true;
}

void base_game::set_ded(size_t x, size_t y)
{
    game_field[y * size.first + x] = false;
}

void base_game::reverse_cell(size_t x, size_t y)
{
    if(game_field[y * size.first + x]) game_field[y * size.first + x] = 0; else game_field[y * size.first + x] = 1;
}

const std::vector<size_t> & base_game::get_field() const
{
    return game_field;
}

const std::pair<size_t, size_t> & base_game::get_size() const
{
    return size;
}

const std::pair<size_t, size_t> & base_game::get_max_size() const
{
    return base_game::max_size;
}

const std::pair<size_t, size_t> & base_game::get_min_size() const
{
    return base_game::min_size;
}

void base_game::set_rule_b(QString b){
    for (size_t i = 0; i < 9; ++i){
        b_rule[i] = 0;
    }
    for (char c: b.toStdString()){
        if (c >= '0' && c <= '9') {
            b_rule[static_cast<int>( c - '0')] = 1;
        }
    }
}

void base_game::set_rule_s(QString s){
    for (size_t i = 0; i < 9; ++i){
        s_rule[i] = 0;
    }
    for (char c: s.toStdString()){
        if (c >= '0' && c <= '9') {
            s_rule[static_cast<int>( c - '0')] = 1;
        }
    }
}
