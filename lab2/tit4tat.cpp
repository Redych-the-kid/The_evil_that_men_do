#include "tit4tat.h"
#include "factory.h"

namespace{
    Strategy *create(){
        return new Tit4tat;
    }
    bool registered = Factory<Strategy, std::string>::get_instance()->register_creator("tit4tat", create);
}

size_t Tit4tat::decide() {
    if(betrayal_happend){
        return DEFECT;
    }
    return COOPERATE;
}

void Tit4tat::add_enemy_desicions(size_t first, size_t second) {
    if (first == DEFECT || second == DEFECT) {
        betrayal_happend = true;
    }
    else{
        betrayal_happend = false;
    }
}