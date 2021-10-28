//
// Created by opezdal on 20.10.2021.
//
#include "cooperate.h"
#include "factory.h"
namespace {
    Strategy * create(){
        return new Cooperate;
    }
    bool registered = Factory<Strategy, std::string>::  get_instance()->register_creator("cooperate", create);
}

size_t Cooperate::decide() {
    return COOPERATE;
}