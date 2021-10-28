#include "random.h"
#include "factory.h"
#include <cstdlib>
#include <ctime>

namespace{
    Strategy *create(){
        return new Random;
    }
    bool registered = Factory<Strategy, std::string> ::get_instance()->register_creator("random",create);
}

size_t Random::decide(){
    std::srand(std::time(nullptr));
    int random = std::rand() / 2;
    if(random % 2 == 0){
        return COOPERATE;
    }
    return DEFECT;
}