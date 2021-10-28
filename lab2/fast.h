#ifndef LAB2_FAST_H
#define LAB2_FAST_H
#include "controller.h"
class Fast_controller : public Controller{
public:
    Fast_controller(std::vector<std::string> &names, size_t steps) : Controller(names, steps){};
    virtual void engage();
};
#endif //LAB2_FAST_H
