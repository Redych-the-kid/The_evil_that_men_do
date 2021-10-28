#include "impostor.h"
#include "factory.h"

namespace {
    Strategy *create(){
        return new Impostor;
    }
    bool registered = Factory<Strategy, std::string>:: get_instance()->register_creator("impostor", create);
}

size_t Impostor::decide() {
    return DEFECT;
}