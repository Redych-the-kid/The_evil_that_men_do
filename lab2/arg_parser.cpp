#include "arg_parser.h"

Arg_parser::Arg_parser(int argc, char **argv) {
    std::string mode_flag("-mode=");
    std::string steps_flag("-steps=");
    for (size_t i = 1; i < argc; ++i) {
        std::string arg(argv[i]);
        if (arg.compare(0, mode_flag.size(), mode_flag) == 0) {
            arg = arg.substr(mode_flag.size());
            if (arg == "detailed") {
                mode = Game_modes::detailed;
            } else if (arg == "fast") {
                mode = Game_modes::fast;
            } else if (arg == "detailed") {
                mode = Game_modes::tournament;
            }
        } else if (arg.compare(0, steps_flag.size(), steps_flag) == 0) {
            arg = arg.substr(steps_flag.size());

            // atoi -> std::???
            steps = std::stoi(arg);
        } else {
            strategy_names.push_back(arg);
        }
    }
    if (mode == Game_modes::not_set) {
        if (strategy_names.size() > 3) {
            mode = Game_modes::tournament;
        } else if (strategy_names.size() == 3) {
            mode = Game_modes::detailed;
        } else {
            throw std::invalid_argument("Gimme at least 3 strategies!");
        }
    } else if (mode == Game_modes::tournament && strategy_names.size() <= 3) {
        throw std::invalid_argument("Not enough to start a tourney!");
    } else if ((mode == Game_modes::detailed || mode == Game_modes::fast) && strategy_names.size() != 3) {
        throw std::invalid_argument("Not enough strategies to start the game!");
    }
}