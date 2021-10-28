#include "fast.h"

void Fast_controller::engage(){
    for(size_t i = 0; i < steps;++i){
        do_iteration(0, 1, 2);
    }
    print_state();
}