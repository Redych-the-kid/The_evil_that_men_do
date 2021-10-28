#ifndef LAB2_RANDOM_H
#define LAB2_RANDOM_H
#include "strategy.h"

class Random: public Strategy{
public:
    virtual size_t decide();
};
#endif //LAB2_RANDOM_H
