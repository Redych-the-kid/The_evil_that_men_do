#ifndef LAB2_COOPERATE_H
#define LAB2_COOPERATE_H
#include "strategy.h"

class Cooperate: public Strategy{
public:
    virtual size_t decide();
};
#endif //LAB2_COOPERATE_H
