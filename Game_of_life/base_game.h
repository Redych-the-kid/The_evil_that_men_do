#ifndef BASE_GAME_H
#define BASE_GAME_H

#include <iostream>
#include <vector>
#include <QString>
#include <QTextStream>
#include <string>
#include <regex>
#include <cmath>
#include <QDebug>

class base_game
{
public:
    struct configs {
        size_t width;
        size_t height;
        QString rule_b;
        QString rule_s;
    };
    base_game(std::pair<size_t, size_t> def_size = {10,10});

    void new_iteration();
    size_t survive(size_t x, size_t y);
    void third_impact(); // reset, but cooler
    void resize(std::pair<size_t, size_t> new_size);
    void set_ded(size_t x, size_t y);
    void set_alive(size_t x, size_t y);
    void reverse_cell(size_t x, size_t y);

    void set_h(size_t height);
    void set_w(size_t width);
    void set_rule_b(QString b);
    void set_rule_s(QString s);

    const std::pair<size_t, size_t> & get_size() const;
    const std::pair<size_t, size_t> & get_max_size() const;
    const std::pair<size_t, size_t> & get_min_size() const;

    const std::vector<size_t> & get_field() const;

    configs load(QTextStream & in);
    void read_rules(std::string line, base_game::configs *con);
    bool read_field_line(std::string line, base_game::configs *con, size_t &pos);
private:
    static std::pair<size_t, size_t> max_size;
    static std::pair<size_t, size_t> min_size;
    std::pair<size_t, size_t> size;
    std::vector<size_t> game_field;
    std::vector<size_t> new_generation;
    std::vector<size_t> b_rule;
    std::vector<size_t> s_rule;
};


#endif // BASE_GAME_H
