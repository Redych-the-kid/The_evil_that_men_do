#include "base_game.h"

std::pair<size_t, size_t> base_game::max_size = std::make_pair(100,100);
std::pair<size_t, size_t> base_game::min_size = std::make_pair(1,1);

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
    if(height < min_size.second){
        size.second = min_size.second;
    }
    else if(height > max_size.second){
        size.second = max_size.second;
    }
    else{
        size.second = height;
    }
}

void base_game::set_w(size_t width)
{
    if(width < min_size.second){
        size.first = min_size.second;
    }
    else if(width > max_size.second){
        size.first = max_size.second;
    }
    else{
        size.first = width;
    }
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

void base_game::read_rules(std::string line, base_game::configs *con){
    std::regex size("x = (\\d+), y = (\\d+)");
    std::smatch match;
    if(!std::regex_search(line, match, size)){
        throw std::exception();
    }
    con->width = std::stoi(match[1].str());
    con->height = std::stoi(match[2].str());
    std::regex rules(", rule = B(\\d+)/S(\\d+)");
    set_h(con->height);
    set_w(con->width);
    if(!std::regex_search(line, match, rules)){
        third_impact();
        return;
    }
    QString q_b = QString::fromStdString(match[1].str());
    QString q_s = QString::fromStdString(match[2].str());
    con->rule_b = q_b;
    con->rule_s = q_s;
    set_rule_b(q_b);
    set_rule_s(q_s);
    third_impact();
}

bool base_game::read_field_line(std::string line, base_game::configs *con, size_t & pos){
    std::string num_string = "";
    size_t width = con->width;
    size_t height = con->height;
    for(char c: line){
        if(std::isdigit(c)){
            num_string.push_back(c);
        }
        else{
            size_t num = std::atoi(num_string.c_str());
            if(num == 0){
                num = 1;
            }
            size_t state = 0;
            if(c == 'b'){
                pos += num;
                num_string = "";
                continue;
            }
            else if(c == 'o'){
                state = 1;
            }
            else if(c == '$'){
                size_t add = (width - (pos % width)) + (width * (num - 1));
                if(pos % width == 0){
                    add -= width;
                }
                pos += add;
                num_string = "";
                continue;
            }
            else if(c == '!'){
                return false;
            }
            else{
                throw std::invalid_argument("Unknow letter!");
                return false;
            }
            if(pos + num >= width * height){
                throw std::out_of_range("Out of range!");
                return false;
            }
            for(size_t i = pos;i < pos + num;++i){
                game_field[i] = state;
            }
            pos += num;
            num_string = "";
        }

    }
    return true;
}

base_game::configs base_game::load(QTextStream & in){
    configs con;
    std::string line;
    bool rules_parsed = false;
    size_t pos = 0;
    while(!in.atEnd()){
        QString q_line = in.readLine();
        line = q_line.toStdString();
        if(line[0] == '#' || line.size() == 0){
            continue;
        }
        if(!rules_parsed){
            read_rules(line, &con);
            rules_parsed = true;
            continue;
        }
        if(!read_field_line(line, &con, pos)){
            break;
        }
    }
    return con;
}
