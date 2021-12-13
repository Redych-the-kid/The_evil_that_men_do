#include <iostream>
#include "controller.h"
#include "fast.h"
#include "detailed.h"
#include "tournament.h"
#include <gtest/gtest.h>
#include "arg_parser.h"

int main(int argc, char **argv) {
    /*Arg_parser arg_parser(argc, argv);
    std::unique_ptr<Controller> magic;
    if (arg_parser.mode == Game_modes::tournament) {
        magic = std::unique_ptr<Controller>(new Tournament_controller(arg_parser.strategy_names, arg_parser.steps));
    } else if (arg_parser.mode == Game_modes::fast) {
        magic = std::unique_ptr<Controller>(new Fast_controller(arg_parser.strategy_names, arg_parser.steps));
    } else if (arg_parser.mode == Game_modes::detailed) {
        magic = std::unique_ptr<Controller>(new Detailed_controller(arg_parser.strategy_names, arg_parser.steps));
    } else {
        printf("Error,desu!");
        return -1;
    }
    magic->engage();
    magic->print_winner();
    */
    testing::InitGoogleTest();
    return RUN_ALL_TESTS();
}