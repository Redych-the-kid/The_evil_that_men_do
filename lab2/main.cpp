#include <iostream>
#include "score_table.h"
#include "controller.h"
#include "fast.h"
#include "detailed.h"
#include "tournament.h"
#include <gtest/gtest.h>

int main() {
    /*std::vector<std::string> names= {"cooperate","impostor","tit4tat"};
    std::unique_ptr<Controller> magic = std::unique_ptr<Controller>(new Tournament_controller(names, 3));
    std::cout << "Who will be scratched from blackbook today?";
    magic->engage();
    magic->print_winner();*/
    testing::InitGoogleTest();
    return RUN_ALL_TESTS();
}