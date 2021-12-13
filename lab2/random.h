#ifndef LAB2_RANDOM_H
#define LAB2_RANDOM_H
#include "strategy.h"

class Random: public Strategy{
public:
    virtual size_t decide();
    virtual void load_config();
private:
    int randomness = 2;
};
#endif //LAB2_RANDOM_H
