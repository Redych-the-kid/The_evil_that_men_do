#ifndef LAB2_DETAILED_H
#define LAB2_DETAILED_H
#include "controller.h"

class Detailed_controller: public Controller{
public:
    Detailed_controller(std::vector<std::string> &names, size_t steps): Controller(names, steps){};
    void engage() override;
    void test_engage();
};

#endif //LAB2_DETAILED_H
