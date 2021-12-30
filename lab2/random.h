#ifndef LAB2_RANDOM_H
#define LAB2_RANDOM_H
#include "strategy.h"

class Random: public Strategy{
public:
    size_t decide() override;
    void load_config() override;
private:
    int randomness = 2;
};
#endif //LAB2_RANDOM_H
