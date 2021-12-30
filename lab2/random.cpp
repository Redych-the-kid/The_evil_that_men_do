#include "random.h"

#include <cstdlib>
#include <ctime>

#include "factory.h"

namespace{
    Strategy *create(){
        return new Random;
    }
    bool registered = Factory<Strategy, std::string> ::get_instance()->register_creator("random",create);
}

size_t Random::decide(){
    std::srand(std::time(nullptr));
    load_config();
    int random = std::rand() / randomness;
    if(random % 2 == 0){
        return COOPERATE;
    }
    return DEFECT;
}

void Random::load_config() {
    std::ifstream file("random.cfg");
    file >> randomness;
}