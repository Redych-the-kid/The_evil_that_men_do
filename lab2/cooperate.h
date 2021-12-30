#ifndef LAB2_COOPERATE_H
#define LAB2_COOPERATE_H
#include "strategy.h"

class Cooperate: public Strategy{
public:
    size_t decide() override;
};
#endif //LAB2_COOPERATE_H
