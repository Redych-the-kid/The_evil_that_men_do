#include "detailed.h"

void Detailed_controller::engage() {
    std::string input;
    std::getline(std::cin, input);
    while(input != "stop"){
        do_iteration(0, 1 , 2);
        print_state();
        std::getline(std::cin, input);
    }
}

void Detailed_controller::test_engage() {
    do_iteration(0, 1, 2);
    print_state();
}