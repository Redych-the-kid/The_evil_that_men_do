#ifndef LAB2_TOURNAMENT_H
#define LAB2_TOURNAMENT_H

#include "controller.h"

class Tournament_controller : public Controller{
public:
    Tournament_controller(std::vector<std::string > &names, size_t steps) : Controller(names, steps){};
    void engage() override;
private:
    void tournament_tick();
};
#endif
