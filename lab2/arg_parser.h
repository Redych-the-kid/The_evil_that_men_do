#ifndef LAB2_ARG_PARSER_H
#define LAB2_ARG_PARSER_H

#include <iostream>
#include <vector>
#include <string>
#define DEFAULT_STEPS 1

enum Game_modes{
    not_set,
    detailed,
    fast,
    tournament
};

class Arg_parser{
public:
    std::vector<std::string> strategy_names;
    size_t steps = DEFAULT_STEPS;
    Game_modes mode = Game_modes::not_set;
    Arg_parser(int argc, char **argv);
};
#endif //LAB2_ARG_PARSER_H
